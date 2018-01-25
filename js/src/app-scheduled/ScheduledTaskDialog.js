import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import Button from 'aui-react/lib/AUIButton';
import Modal from 'aui-react/lib/AUIDialog';

import {TaskActionCreators} from './scheduled.reducer';

import {AUIRequired} from '../common/aui-components';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {scheduledTaskService} from '../service/services';
import {Editor} from '../common/Editor';
import {getMarkers} from '../common/error';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {AsyncPicker} from '../common/AsyncPicker';
import {JqlInput} from '../common/JqlInput';
import {Error} from '../common/forms/Error';


const types = {
    BASIC_SCRIPT: {
        name: 'Basic script',
        fields: ['scriptBody']
    },
    ISSUE_JQL_SCRIPT: {
        name: 'JQL issue script',
        fields: ['issueJql', 'scriptBody']
    },
    DOCUMENT_ISSUE_JQL_SCRIPT: {
        name: 'JQL document issue script',
        fields: ['issueJql', 'scriptBody']
    },
    ISSUE_JQL_TRANSITION: {
        name: 'JQL issue transition',
        fields: ['issueJql', 'workflowAction', 'transitionOptions']
    }
};

const typeList = Object
    .keys(types)
    .map(key => {
        return {
            ...(types[key]),
            key
        };
    });

@connect(
    null,
    TaskActionCreators
)
export class ScheduledTaskDialog extends React.Component {
    static propTypes = {
        isNew: PropTypes.bool.isRequired,
        onClose: PropTypes.func.isRequired,
        id: PropTypes.number,
        updateTask: PropTypes.func.isRequired,
        addTask: PropTypes.func.isRequired
    };

    state = {
        ready: false,
        values: null
    };

    componentWillReceiveProps(nextProps) {
        this._init(nextProps);
    }

    componentDidMount() {
        this._init(this.props);
    }

    _init = props => {
        if (props.isNew) {
            this.setState({
                ready: true,
                values: new Map({
                    name: '',
                    scriptBody: ''
                }),
                error: null
            });
        } else {
            this.setState({
                ready: false,
                values: null,
                error: null
            });

            scheduledTaskService //todo
                .get(props.id)
                .then(task => {
                    this.setState({
                        values: new Map({
                            name: task.name,
                            scriptBody: task.scriptBody,
                            scheduleExpression: task.scheduleExpression,
                            userKey: task.user,
                            type: task.type,
                            issueJql: task.issueJql,
                            comment: ''
                        }),
                        ready: true
                    });
                });
        }
    };

    _handleError = (error) => {
        const {response} = error;

        if (response.status === 400) {
            this.setState({error: response.data});
        } else {
            throw error;
        }
    };

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        const {isNew, id, onClose} = this.props;

        const jsData = this.state.values.toJS();
        const data = {
            ...jsData,
            userKey: jsData.userKey ? jsData.userKey.value : null
        };

        if (isNew) {
            scheduledTaskService
                .create(data)
                .then(
                    script => {
                        onClose();
                        this.props.addTask(script);
                    },
                    this._handleError
                );
        } else {
            scheduledTaskService
                .update(id, data)
                .then(
                    script => {
                        onClose();
                        this.props.updateTask(script);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field, value) => {
        this.setState(state => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setTextValue = (field) => (event) => this.mutateValue(field, event.target.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    _renderField = (fieldName, error) => {
        const {values} = this.state;

        switch (fieldName) {
            case 'scriptBody':
                let markers = null;

                if (error && error.field === fieldName) {
                    if (Array.isArray(error.error)) {
                        const errors = error.error.filter(e => e);
                        markers = getMarkers(errors);
                        error = {
                            field: fieldName,
                            messages: errors.map(error => error.message)
                        };
                    }
                }

                return (
                    <div className="field-group" key={fieldName}>
                        <label>
                            {FieldMessages.scriptCode}
                            <AUIRequired/>
                        </label>
                        <Editor
                            mode="groovy"
                            decorated={true}

                            value={values.get(fieldName) || ''}
                            onChange={this._setObjectValue(fieldName)}

                            markers={markers}
                        />
                        <Error error={error} thisField={fieldName}/>
                    </div>
                );
            case 'issueJql':
                //todo: jql select
                return (
                    <div className="field-group" key={fieldName}>
                        <label htmlFor="task-dialog-jql">
                            {FieldMessages.issueJql}<AUIRequired/>
                        </label>
                        <JqlInput
                            id="task-dialog-jql"

                            value={values.get(fieldName)}
                            onChange={this._setTextValue(fieldName)}
                        />
                        <div className="description">
                            <span className="aui-icon aui-icon-warning"/>{' '}
                            {ScheduledTaskMessages.jqlLimitDescription(1000) /*todo: insert value from config when its configurable*/}
                        </div>
                        <Error error={error} thisField={fieldName}/>
                    </div>
                );
            case 'workflowAction':
                const workflow = values.get('issueWorkflowName');
                return (
                    <div>
                        <div className="field-group" key={fieldName}>
                            <label>
                                Workflow<AUIRequired/>
                            </label>
                            <AsyncPicker
                                src={`${getPluginBaseUrl()}/jira-api/workflowPicker`}
                                value={workflow}
                                onChange={this._setObjectValue('issueWorkflowName')}
                            />
                            <Error error={error} thisField={fieldName}/>
                        </div>
                        {workflow &&
                            <div className="field-group" key={fieldName}>
                                <label>
                                    Workflow action<AUIRequired/>
                                </label>
                                <AsyncPicker
                                    src={`${getPluginBaseUrl()}/jira-api/workflowActionPicker/${workflow.value}`}
                                    value={values.get('issueWorkflowActionId')}
                                    onChange={this._setObjectValue('issueWorkflowActionId')}
                                />
                                <Error error={error} thisField={fieldName}/>
                            </div>
                        }
                    </div>
                );
            case 'transitionOptions':
                //todo: link controls
                return (
                    <fieldset className="group">
                    <legend>Transition options</legend>
                    <div className="checkbox">
                        <input className="checkbox" type="checkbox" name="checkBoxOne" id="task-dialog-transition-skip-conditions"/>
                        <label htmlFor="task-dialog-transition-skip-conditions">Skip conditions</label>
                    </div>
                    <div className="checkbox">
                        <input className="checkbox" type="checkbox" name="checkBoxOne" id="task-dialog-transition-skip-validators"/>
                        <label htmlFor="task-dialog-transition-skip-validators">Skip validators</label>
                    </div>
                    <div className="checkbox">
                        <input className="checkbox" type="checkbox" name="checkBoxOne" id="task-dialog-transition-skip-permissions"/>
                        <label htmlFor="task-dialog-transition-skip-permissions">Skip permissions</label>
                    </div>
                    </fieldset>
                );
            default:
                return <div key={fieldName}>NOT IMPLEMENTED</div>;
        }
    };

    render() {
        const {onClose, isNew} = this.props;
        const {ready, values, error} = this.state;

        let body = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            const currentType = values.get('type');

            body =
                <form className="aui" onSubmit={this._onSubmit}>
                    <Error error={error}/>

                    <div className="field-group">
                        <label htmlFor="task-dialog-name">
                            {FieldMessages.name}
                            <AUIRequired/>
                        </label>
                        <input
                            type="text"
                            className="text full-width-field"
                            id="task-dialog-name"
                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                        <Error error={error} thisField="name"/>
                    </div>
                    <div className="field-group">
                        <label htmlFor="task-dialog-schedule">
                            {FieldMessages.schedule}<AUIRequired/>
                        </label>
                        <input
                            type="text"
                            className="text full-width-field"
                            id="task-dialog-schedule"
                            value={values.get('scheduleExpression') || ''}
                            onChange={this._setTextValue('scheduleExpression')}
                        />
                        <Error error={error} thisField="scheduleExpression"/>
                    </div>
                    <div className="field-group">
                        <label>
                            {ScheduledTaskMessages.runAs}<AUIRequired/>
                        </label>
                        <AsyncPicker
                            src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                            onChange={this._setObjectValue('userKey')}
                            value={values.get('userKey')}
                            className="full-width-field"
                        />
                        <Error error={error} thisField="userKey"/>
                    </div>
                    <fieldset className="group">
                        <legend>{FieldMessages.type}<AUIRequired/></legend>
                        {typeList.map(type =>
                            <div className="radio" key={type.key}>
                                <input
                                    className="radio"
                                    type="radio"
                                    name="task-dialog-type"

                                    id={`task-dialog-type-${type.key}`}
                                    value={type.key}
                                    checked={values.get('type') === type.key}
                                    onChange={this._setTextValue('type')}
                                />
                                <label htmlFor={`task-dialog-type-${type.key}`}>
                                    {type.name}
                                </label>
                            </div>
                        )}
                        <Error error={error} thisField="type"/>
                    </fieldset>
                    {currentType && types[currentType].fields.map(field => this._renderField(field, error))}
                    {!isNew && <div className="field-group">
                        <label htmlFor="task-dialog-comment">
                            {FieldMessages.comment}
                            <AUIRequired/>
                        </label>
                        <textarea
                            id="task-dialog-comment"
                            className="textarea full-width-field"

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                        <Error error={error} thisField="comment"/>
                    </div> }
                </form>;
        }

        return <Modal
            size="xlarge"
            titleContent={`${isNew ? RestMessages.createScript : RestMessages.updateScript}`}
            onClose={onClose}
            footerActionContent={[
                <Button key="create" onClick={this._onSubmit}>
                    {isNew ? CommonMessages.create : CommonMessages.update}
                </Button>,
                <Button key="close" type="link" onClick={onClose}>{CommonMessages.cancel}</Button>
            ]}
            type="modal"
            styles={{zIndex: '3000'}}
        >
            {body}
        </Modal>;
    }
}
