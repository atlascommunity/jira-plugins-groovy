//@flow
import * as React from 'react';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import Button from '@atlaskit/button';
import Lozenge from '@atlaskit/lozenge';

import VidPlayIcon from '@atlaskit/icon/glyph/vid-play'; //todo: better icon
import CodeIcon from '@atlaskit/icon/glyph/code';

import {RunDialog} from './RunDialog';

import type {AdminScriptType} from './types';

import {ItemActionCreators, WatchActionCreators} from '../common/redux';

import {adminScriptService} from '../service/services';

import {WatchableScript} from '../common/script/WatchableScript';

import type {ScriptComponentProps} from '../common/script-list/types';
import {AdminScriptMessages} from '../i18n/admin.i18n';
import {CommonMessages} from '../i18n/common.i18n';


const ConnectedWatchableScript = connect(
    memoizeOne(
        (state: *): * => {
            return {
                watches: state.watches
            };
        }
    ),
    WatchActionCreators
)(WatchableScript);

const {deleteItem} = ItemActionCreators;

type Props = ScriptComponentProps<AdminScriptType> & {
    deleteItem: typeof deleteItem
};

type State = {
    isRunning: boolean
};

class AdminScriptInternal extends React.PureComponent<Props, State> {
    state = {
        isRunning: false
    };

    _toggleDialog = () => this.setState((state: State): * => {
        return {
            isRunning: !state.isRunning
        };
    });

    _onEdit = () => this.props.onEdit(this.props.script.id);

    _delete = () => {
        const script = this.props.script;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${script.name}"?`)) {
            adminScriptService.deleteScript(script.id).then(() => this.props.deleteItem(script.id));
        }
    };

    _getScript = memoizeOne(
        (script) => script.builtIn ? null : {
            id: script.uuid,
            name: script.name,
            description: script.description,
            inline: true,
            scriptBody: script.scriptBody,
            changelogs: script.changelogs,
            errorCount: script.errorCount
        }
    );

    render(): React.Node {
        const {script} = this.props;
        const {isRunning} = this.state;
        const {builtIn} = script;

        return (
            <ConnectedWatchableScript
                entityId={script.id}
                entityType="ADMIN_SCRIPT"
                isUnwatchable={builtIn}

                title={builtIn ?
                    <div className="flex-row space-between">
                        <div className="flex-vertical-middle flex-none">
                            <CodeIcon label=""/>
                        </div>
                        <div className="flex-vertical-middle flex-none">
                            <Lozenge appearance="inprogress">
                                {AdminScriptMessages.builtIn}
                            </Lozenge>
                        </div>
                        <div className="flex-vertical-middle">
                            <h3 title={script.name}>
                                {script.name}
                            </h3>
                        </div>
                        <div className="flex-grow"/>
                    </div>
                    : undefined
                }

                script={this._getScript(script)}

                withChangelog={true}

                onEdit={!builtIn ? this._onEdit : undefined}
                onDelete={!builtIn ? this._delete : undefined}

                additionalPrimaryButtons={[
                    <Button appearance="subtle" iconBefore={<VidPlayIcon label="run"/>} onClick={this._toggleDialog}>
                        {CommonMessages.run}
                    </Button>
                ]}
            >
                {isRunning && <RunDialog script={script} onClose={this._toggleDialog}/>}
            </ConnectedWatchableScript>
        );
    }
}

export const AdminScript = connect(null, { deleteItem })(AdminScriptInternal);
