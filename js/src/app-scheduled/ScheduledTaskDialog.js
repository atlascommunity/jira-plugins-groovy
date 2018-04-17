import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {AkFieldRadioGroup} from '@atlaskit/field-radio-group';
import {CheckboxStateless, CheckboxGroup} from '@atlaskit/checkbox';
import {colors} from '@atlaskit/theme';
import {Label} from '@atlaskit/field-base';

import WarningIcon from '@atlaskit/icon/glyph/warning';

import {TaskActionCreators} from './scheduled.reducer';
import {types, typeList} from './types';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';

import {scheduledTaskService} from '../service/services';
import {getMarkers} from '../common/error';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {Error} from '../common/forms/Error';
import {Bindings} from '../common/bindings';
import {AsyncPicker} from '../common/ak/AsyncPicker';
import {EditorField} from '../common/ak/EditorField';
import {JqlInput} from '../common/ak/JqlInput';
import {FieldError} from '../common/ak/FieldError';


const issueBindings = [Bindings.issue];
const emptyBindings = [];

function getValue(option) {
    return option ? option.value : null;
}

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
                    transitionOptions: {},
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

            scheduledTaskService
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
                            issueWorkflowName: task.issueWorkflow,
                            issueWorkflowActionId: task.issueWorkflowAction,
                            transitionOptions: task.transitionOptions || {},
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
            userKey: getValue(jsData.userKey),
            issueWorkflowName: getValue(jsData.issueWorkflowName),
            issueWorkflowActionId: getValue(jsData.issueWorkflowActionId)
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

    _toggleTransitionOption2 = (e) => {
        const option = e.currentTarget.value;

        this.setState(state => {
            const options = state.values.get('transitionOptions');

            return {
                values: state.values.set('transitionOptions', {
                    ...options,
                    [option]: !options[option]
                })
            };
        });
    };

    _renderField = (fieldName, error) => {
        const {values} = this.state;

        let errorMessage = null;
        let errorField = null;

        if (error) {
            errorMessage = error.message || error.messages;
            errorField = error.field;
        }

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

                const currentType = values.get('type');

                let bindings = null;

                switch (currentType) {
                    case 'ISSUE_JQL_SCRIPT':
                    case 'DOCUMENT_ISSUE_JQL_SCRIPT':
                        bindings = issueBindings;
                        break;
                    default:
                        bindings = emptyBindings;
                }

                return (
                    <EditorField
                        key={fieldName}

                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                        markers={markers}

                        bindings={bindings}

                        value={values.get(fieldName) || ''}
                        onChange={this._setObjectValue(fieldName)}
                    />
                );
            case 'issueJql':
                return (
                    <div key={fieldName}>
                        <JqlInput
                            label={FieldMessages.issueJql}
                            isRequired={true}

                            shouldFitContainer={true}

                            value={values.get(fieldName) || ''}
                            onChange={this._setTextValue(fieldName)}

                            isInvalid={errorField === fieldName}
                            invalidMessage={errorField === fieldName ? errorMessage : ''}
                        />
                        <div className="ak-description">
                            <WarningIcon size="small" label="" primaryColor={colors.Y500}/>
                            {' '}
                            {ScheduledTaskMessages.jqlLimitDescription(1000) /*todo: insert value from config when its configurable*/}
                        </div>
                        {['ISSUE_JQL_SCRIPT', 'DOCUMENT_ISSUE_JQL_SCRIPT'].includes(values.get('type')) &&
                            <div className="ak-description">
                                <WarningIcon size="small" label="" primaryColor={colors.Y500}/>
                                {' '}
                                {ScheduledTaskMessages.jqlScriptDescription}
                            </div>
                        }
                        <Error error={error} thisField={fieldName}/>
                    </div>
                );
            case 'workflowAction':
                const workflow = values.get('issueWorkflowName');
                return (
                    <div key={fieldName}>
                        <AsyncPicker
                            label={FieldMessages.workflow}
                            isRequired={true}

                            src={`${getPluginBaseUrl()}/jira-api/workflowPicker`}
                            value={workflow}
                            onChange={this._setObjectValue('issueWorkflowName')}

                            isInvalid={errorField === fieldName}
                            invalidMessage={errorField === fieldName ? errorMessage : ''}
                        />
                        {workflow &&
                            <AsyncPicker
                                label={FieldMessages.workflowAction}
                                isRequired={true}

                                src={`${getPluginBaseUrl()}/jira-api/workflowActionPicker/${workflow.value}`}
                                value={values.get('issueWorkflowActionId')}
                                onChange={this._setObjectValue('issueWorkflowActionId')}

                                isInvalid={errorField === fieldName}
                                invalidMessage={errorField === fieldName ? errorMessage : ''}
                            />
                        }
                    </div>
                );
            case 'transitionOptions':
                const value = values.get(fieldName);
                return (
                    <div key={fieldName}>
                        <Label label={ScheduledTaskMessages.transitionOptions}/>
                        <CheckboxGroup>
                            <CheckboxStateless
                                isChecked={value.skipConditions || false}
                                onChange={this._toggleTransitionOption2}
                                label={ScheduledTaskMessages.transitionOption.skipConditions}
                                value="skipConditions"
                                name="transition-options"
                            />
                            <CheckboxStateless
                                isChecked={value.skipValidators || false}
                                onChange={this._toggleTransitionOption2}
                                label={ScheduledTaskMessages.transitionOption.skipValidators}
                                value="skipValidators"
                                name="transition-options"
                            />
                            <CheckboxStateless
                                isChecked={value.skipPermissions || false}
                                onChange={this._toggleTransitionOption2}
                                label={ScheduledTaskMessages.transitionOption.skipPermissions}
                                value="skipPermissions"
                                name="transition-options"
                            />
                        </CheckboxGroup>
                    </div>
                );
            default:
                return <div key={fieldName}>{'NOT IMPLEMENTED'}</div>;
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

            let errorMessage = null;
            let errorField = null;

            if (error) {
                errorMessage = error.message || error.messages;
                errorField = error.field;
            }

            body =
                <div className="flex-column">
                    <Error error={error}/>

                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={values.get('name') || ''}
                        onChange={this._setTextValue('name')}
                    />
                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'scheduleExpression'}
                        invalidMessage={errorField === 'scheduleExpression' ? errorMessage : null}

                        label={FieldMessages.schedule}
                        value={values.get('scheduleExpression') || ''}
                        onChange={this._setTextValue('scheduleExpression')}
                    />

                    <AsyncPicker
                        label={ScheduledTaskMessages.runAs}
                        isRequired={true}

                        src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                        onChange={this._setObjectValue('userKey')}
                        value={values.get('userKey')}

                        isInvalid={errorField === 'userKey'}
                        invalidMessage={errorField === 'userKey' ? errorMessage : ''}
                    />

                    <AkFieldRadioGroup
                        label={FieldMessages.type}
                        isRequired={true}

                        items={typeList.map(type => {
                            return {
                                label: type.name,
                                value: type.key,
                                isSelected: values.get('type') === type.key
                            };
                        })}
                        value={values.get('type')}
                        onRadioChange={this._setTextValue('type')}
                    />
                    {errorField === 'type' && <FieldError error={errorMessage}/>}

                    {currentType && types[currentType].fields.map(field => this._renderField(field, error))}
                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        required={!isNew}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorField === 'comment' ? errorMessage : null}

                        label={FieldMessages.comment}
                        value={values.get('comment') || ''}
                        onChange={this._setTextValue('comment')}
                    />
                </div>;
        }

        return <ModalDialog
            width="x-large"
            scrollBehavior="outside"
            heading={`${isNew ? RestMessages.createScript : RestMessages.updateScript}`}
            onClose={onClose}
            actions={[
                {
                    text: isNew ? CommonMessages.create : CommonMessages.update,
                    onClick: this._onSubmit,
                },
                {
                    text: CommonMessages.cancel,
                    onClick: onClose,
                }
            ]}
        >
            {body}
        </ModalDialog>;
    }
}
