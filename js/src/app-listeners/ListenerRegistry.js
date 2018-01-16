import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

import {ListenerActionCreators} from './listeners.reducer';

import {ConditionModel, ListenerModel, conditions} from '../model/listener.model';

import {listenerService} from '../service/services';

import {ListenerMessages} from '../i18n/listener.i18n';
import {Script} from '../common/Script';

import './ListenerRegistry.less';


export class ListenerRegistry extends React.Component {
    static propTypes = {
        listeners: PropTypes.arrayOf(ListenerModel).isRequired,
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew, id) => () => this.props.triggerDialog(isNew, id);

    render() {
        const {listeners} = this.props;

        return (
            <div className="flex-column">
                <div>
                    <Button icon="add" type="primary" onClick={this._triggerDialog(true)}>
                        {ListenerMessages.addListener}
                    </Button>
                </div>
                <div className="flex-column">
                    <div>
                        {listeners.map(listener =>
                            <Listener
                                key={listener.id}
                                listener={listener}
                                onEdit={this._triggerDialog(false, listener.id)}
                            />
                        )}
                    </div>
                    {!listeners.length && <Message type="info" title={ListenerMessages.noListeners}>{ListenerMessages.noListeners}</Message>}
                </div>
            </div>
        );
    }
}

@connect(
    () => { return {}; },
    ListenerActionCreators
)
class Listener extends React.Component {
    static propTypes = {
        listener: ListenerModel.isRequired,
        onEdit: PropTypes.func.isRequired,
        deleteListener: PropTypes.func.isRequired
    };

    _delete = () => {
        const listener = this.props.listener;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${listener.name}"?`)) {
            listenerService.deleteListener(listener.id).then(() => this.props.deleteListener(listener.id));
        }
    };

    render() {
        const {listener, onEdit} = this.props;

        return <div className="flex-column">
            <Script
                script={{
                    id: listener.uuid,
                    name: listener.name,
                    inline: true,
                    scriptBody: listener.scriptBody,
                    changelogs: listener.changelogs
                }}

                withChangelog={true}
                editable={true}
                onEdit={onEdit}
                onDelete={this._delete}
            >
                <Condition condition={listener.condition}/>
            </Script>
        </div>;
    }
}


@connect(
    state => {
        return {
            projects: state.projects,
            eventTypes: state.eventTypes
        };
    }
)
class Condition extends React.Component {
    static propTypes = {
        condition: ConditionModel.isRequired,
        projects: PropTypes.object.isRequired,
        eventTypes: PropTypes.object.isRequired
    };

    render() {
        const {condition, projects, eventTypes} = this.props;

        let vertical = true;
        let conditionBody = null;
        switch (condition.type) {
            case 'AND':
            case 'OR':
                vertical = false;
                conditionBody = condition.children
                    .map(condition => <Condition
                        key={condition.key}
                        condition={condition}
                        projects={projects}
                        eventTypes={eventTypes}
                    />);
                break;
            case 'CLASS_NAME':
                conditionBody = condition.className;
                break;
            case 'ISSUE_PROJECT':
                conditionBody = condition.entityIds.map(id =>
                    <div key={id}>{projects[id] || id}</div>
                );
                break;
            case 'ISSUE_EVENT_TYPE':
                conditionBody = condition.entityIds.map(id =>
                    <div key={id}>{eventTypes[id] || id}</div>
                );
                break;
            default:
                conditionBody = 'not implemented'; //todo
        }

        return (
            <div className={`Condition ${vertical ? 'flex-column' : 'flex-row'}`}>
                <div className="ConditionName">
                    <strong>{conditions[condition.type].name}</strong>
                </div>
                <div className="ConditionBody">
                    {conditionBody}
                </div>
            </div>
        );
    }
}
