import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {ListenerActionCreators} from './listeners.reducer';

import {ListenerModel} from '../model/listener.model';

import {listenerService} from '../service/services';

import {ListenerMessages, ListenerTypeMessages} from '../i18n/listener.i18n';
import {CommonMessages, FieldMessages, TitleMessages} from '../i18n/common.i18n';

import {Script, ScriptParameters} from '../common/Script';
import {InfoMessage} from '../common/ak/InfoMessage';

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
            <Page>
                <PageHeader
                    actions={
                        <Button
                            appearance="primary"
                            onClick={this._triggerDialog(true)}
                        >
                            {ListenerMessages.addListener}
                        </Button>
                    }
                >
                    {TitleMessages.listeners}
                </PageHeader>

                <div className="page-content ScriptList">
                    {listeners.map(listener =>
                        <Listener
                            key={listener.id}
                            listener={listener}
                            onEdit={this._triggerDialog(false, listener.id)}
                        />
                    )}
                    {!listeners.length && <InfoMessage title={ListenerMessages.noListeners}/>}
                </div>
            </Page>
        );
    }
}

@connect(
    memoizeOne(
        (state) => {
            return {
                projects: state.projects,
                eventTypes: state.eventTypes
            };
        }
    ),
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

    _getParams = memoizeOne(
        (projects, eventTypes, condition) => {
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
                        condition.typeIds.map(id => eventTypes[id] || id).join(', ') :
                        <div className="muted-text">{CommonMessages.notSpecified}</div>
                });
                params.push({
                    label: FieldMessages.projects,
                    value: (
                        <div className="flex-column">
                            {condition.projectIds.length ?
                                condition.projectIds.map(id => <div key={id}>{projects[id] || id}</div>) :
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
        const {listener, projects, eventTypes, onEdit} = this.props;

        return (
            <Script
                script={{
                    id: listener.uuid,
                    name: listener.name,
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
