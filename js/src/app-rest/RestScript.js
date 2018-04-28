//@flow
import * as React from 'react';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import type {RestScriptType} from './types';

import {ScriptParameters} from '../common/script';

import {restService} from '../service/services';
import {getPluginBaseUrl} from '../service/ajaxHelper';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {ItemActionCreators, WatchActionCreators} from '../common/redux';
import type {VoidCallback} from '../common/types';
import {WatchableScript} from '../common/script/WatchableScript';


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

type Props = {
    script: RestScriptType,
    onEdit: VoidCallback,
    deleteItem: typeof ItemActionCreators.deleteItem
}

class RestScriptInternal extends React.PureComponent<Props> {
    _delete = () => {
        const script = this.props.script;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${script.name}"?`)) {
            restService.deleteScript(script.id).then(() => this.props.deleteItem(script.id));
        }
    };

    render(): React.Node {
        const {script, onEdit} = this.props;

        const url = `${getPluginBaseUrl()}/custom/${script.name}`;

        return (
            <ConnectedWatchableScript
                entityId={script.id}
                entityType="REGISTRY_SCRIPT"

                script={{
                    id: script.uuid,
                    name: script.name,
                    inline: true,
                    scriptBody: script.scriptBody,
                    changelogs: script.changelogs,
                    errorCount: script.errorCount
                }}

                withChangelog={true}

                onEdit={onEdit}
                onDelete={this._delete}
            >
                <ScriptParameters
                    params={[
                        {
                            label: 'URL',
                            value: <a href={url}>{url}</a>
                        },
                        {
                            label: FieldMessages.httpMethods,
                            value: script.methods.join(', ')
                        },
                        {
                            label: FieldMessages.groups,
                            value: script.groups.length ?
                                script.groups.join(', ') :
                                <div className="muted-text">{CommonMessages.notSpecified}</div>
                        }
                    ]}
                />
            </ConnectedWatchableScript>
        );
    }
}

export const RestScript = connect(null, ItemActionCreators)(RestScriptInternal);
