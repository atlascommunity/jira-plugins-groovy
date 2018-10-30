//@flow
import React from 'react';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import Button from '@atlaskit/button';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';

import type {GlobalObjectScriptType} from './types';

import {WatchActionCreators} from '../common/redux';

import {adminScriptService} from '../service';

import {WatchableScript} from '../common/script/WatchableScript';

import type {ScriptComponentProps} from '../common/script-list/types';
import {CommonMessages} from '../i18n/common.i18n';
import {RouterLink} from '../common/ak/RouterLink';


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

type Props = ScriptComponentProps<GlobalObjectScriptType>;

type State = {
    isRunning: boolean
};

export class GlobalObjectScript extends React.PureComponent<Props, State> {
    static defaultProps = {
        collapsible: true
    };

    state = {
        isRunning: false
    };

    _toggleDialog = () => this.setState((state: State): * => {
        return {
            isRunning: !state.isRunning
        };
    });

    _delete = () => this.props.onDelete && this.props.onDelete(
        this.props.script.id,
        this.props.script.name,
        () => adminScriptService.deleteScript(this.props.script.id)
    );

    _getScript = memoizeOne(
        (script) => ({
            id: script.uuid,
            name: script.name,
            description: script.description,
            inline: true,
            scriptBody: script.scriptBody,
            changelogs: script.changelogs,
            errorCount: script.errorCount,
            warningCount: script.warningCount
        })
    );

    render() {
        const {script, collapsible, focused} = this.props;

            /**/

        return (
            <ConnectedWatchableScript
                entityId={script.id}
                entityType="ADMIN_SCRIPT"

                script={this._getScript(script)}
                withChangelog={true}
                collapsible={collapsible}
                focused={focused}

                onDelete={this._delete}

                additionalPrimaryButtons={[
                    <Button
                        key="edit"
                        appearance="subtle"
                        iconBefore={<EditFilledIcon label=""/>}

                        component={RouterLink}
                        href={`/go/${script.id}/edit`}
                    />
                ]}

                dropdownItems={[
                    {
                        label: CommonMessages.permalink,
                        href: `/go/${script.id}/view`,
                        linkComponent: RouterLink
                    }
                ]}
            />
        );
    }
}
