//@flow
import React from 'react';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import type {ConditionType, ListenerType} from './types';

import {WatchActionCreators} from '../common/redux';

import {ListenerTypeMessages} from '../i18n/listener.i18n';
import {CommonMessages, FieldMessages} from '../i18n/common.i18n';

import {ScriptParameters} from '../common/script';
import {WatchableScript} from '../common/script/WatchableScript';

import type {ObjectMap} from '../common/types';
import type {ScriptParam} from '../common/script/ScriptParameters';
import type {ScriptComponentProps} from '../common/script-list/types';

import './ListenerRegistry.less';
import {listenerService} from '../service/services';
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

type ConnectProps = {
    projects: ObjectMap,
    eventTypes: ObjectMap
};

type Props = ScriptComponentProps<ListenerType> & ConnectProps;

class ListenerInternal extends React.PureComponent<Props> {
    static defaultProps = {
        collapsible: true
    };

    _edit = () => this.props.onEdit && this.props.onEdit(this.props.script.id);

    _delete = () => this.props.onDelete && this.props.onDelete(
        this.props.script.id,
        this.props.script.name,
        () => listenerService.deleteListener(this.props.script.id)
    );

    _getParams = memoizeOne(
        (projects: ObjectMap, eventTypes: ObjectMap, condition: ConditionType): Array<ScriptParam> => {
            const params = [
                {
                    label: FieldMessages.type,
                    value: ListenerTypeMessages[condition.type]
                }
            ];

            if (condition.type === 'CLASS_NAME') {
                params.push({
                    label: FieldMessages.name,
                    value: condition.className
                });
            } else if (condition.type === 'ISSUE') {
                params.push({
                    label: FieldMessages.eventTypes,
                    value: condition.typeIds.length ?
                        condition.typeIds.map(id => eventTypes[id.toString()] || id).join(', ') :
                        <div className="muted-text">{CommonMessages.notSpecified}</div>
                });
                params.push({
                    label: FieldMessages.projects,
                    value: (
                        <div className="flex-column">
                            {condition.projectIds.length ?
                                condition.projectIds.map(id => <div key={id}>{projects[id.toString()] || id}</div>) :
                                <div className="muted-text">{CommonMessages.notSpecified}</div>
                            }
                        </div>
                    )
                });
            }

            return params;
        }
    );

    render() {
        const {script, collapsible, projects, eventTypes} = this.props;

        return (
            <ConnectedWatchableScript
                entityId={script.id}
                entityType={'LISTENER'}

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

                onEdit={this._edit}
                onDelete={this._delete}

                dropdownItems={[
                    {
                        label: CommonMessages.permalink,
                        href: `/listeners/${script.id}/view`,
                        linkComponent: RouterLink
                    }
                ]}
            >
                <ScriptParameters params={this._getParams(projects, eventTypes, script.condition)}/>
            </ConnectedWatchableScript>
        );
    }
}

export const Listener = connect(
    memoizeOne(
        (state: *): ConnectProps => {
            return {
                projects: state.projects,
                eventTypes: state.eventTypes
            };
        }
    )
)(ListenerInternal);
