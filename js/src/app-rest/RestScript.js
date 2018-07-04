//@flow
import React from 'react';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import type {RestScriptType} from './types';

import {ScriptParameters} from '../common/script';

import {getPluginBaseUrl} from '../service/ajaxHelper';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';

import {WatchActionCreators} from '../common/redux';

import {restService} from '../service/services';

import {WatchableScript} from '../common/script/WatchableScript';

import type {ScriptComponentProps} from '../common/script-list/types';
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

type Props = ScriptComponentProps<RestScriptType>;

export class RestScript extends React.PureComponent<Props> {
    static defaultProps = {
        collapsible: true
    };

    _onEdit = () => this.props.onEdit && this.props.onEdit(this.props.script.id);

    _delete = () => this.props.onDelete && this.props.onDelete(
        this.props.script.id,
        this.props.script.name,
        () => restService.deleteScript(this.props.script.id)
    );

    render() {
        const {script, collapsible} = this.props;

        const url = `${getPluginBaseUrl()}/custom/${script.name}`;

        return (
            <ConnectedWatchableScript
                entityId={script.id}
                entityType="REGISTRY_SCRIPT"

                script={{
                    id: script.uuid,
                    name: script.name,
                    description: script.description,
                    inline: true,
                    scriptBody: script.scriptBody,
                    changelogs: script.changelogs,
                    errorCount: script.errorCount
                }}

                withChangelog={true}
                collapsible={collapsible}

                onEdit={this._onEdit}
                onDelete={this._delete}

                dropdownItems={[
                    {
                        label: CommonMessages.permalink,
                        href: `/rest/${script.id}/view`,
                        linkComponent: RouterLink
                    }
                ]}
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
