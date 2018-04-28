//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import type {ConditionType, ListenerType} from './types';

import {ListenerModel} from '../model/listener.model';

import {listenerService} from '../service/services';

import {ItemActionCreators} from '../common/redux';

import {ListenerTypeMessages} from '../i18n/listener.i18n';
import {CommonMessages, FieldMessages} from '../i18n/common.i18n';

import Script, {ScriptParameters} from '../common/script';

import type {ObjectMap} from '../common/types';
import type {ScriptParam} from '../common/script/ScriptParameters';

import './ListenerRegistry.less';


type Props = {
    listener: ListenerType,
    onEdit: () => void,
    deleteItem: typeof ItemActionCreators.deleteItem,
    projects: ObjectMap,
    eventTypes: ObjectMap
};

class ListenerInternal extends React.PureComponent<Props> {
    static propTypes = {
        listener: ListenerModel.isRequired,
        onEdit: PropTypes.func.isRequired,
        deleteItem: PropTypes.func.isRequired
    };

    _delete = () => {
        const listener = this.props.listener;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${listener.name}"?`)) {
            listenerService.deleteListener(listener.id).then(() => this.props.deleteItem(listener.id));
        }
    };

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

    render(): React.Node {
        const {listener, projects, eventTypes, onEdit} = this.props;

        return (
            <Script
                script={{
                    id: listener.uuid,
                    name: listener.name,
                    description: listener.description,
                    inline: true,
                    scriptBody: listener.scriptBody,
                    changelogs: listener.changelogs,
                    errorCount: listener.errorCount
                }}

                withChangelog={true}
                onEdit={onEdit}
                onDelete={this._delete}
            >
                <ScriptParameters params={this._getParams(projects, eventTypes, listener.condition)}/>
            </Script>
        );
    }
}

export const Listener = connect(
    memoizeOne(
        (state: *): * => {
            return {
                projects: state.projects,
                eventTypes: state.eventTypes
            };
        }
    ),
    ItemActionCreators
)(ListenerInternal);
