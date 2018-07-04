//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';

import {Record, type RecordOf, type RecordFactory} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {AkFieldRadioGroup} from '@atlaskit/field-radio-group';
import {CheckboxStateless, CheckboxGroup} from '@atlaskit/checkbox';
import {colors} from '@atlaskit/theme';
import {Label} from '@atlaskit/field-base';

import type {ItemPropType} from '@atlaskit/field-radio-group/dist/cjs/types';

import WarningIcon from '@atlaskit/icon/glyph/warning';

import {types, typeList} from './types';
import type {
    ScheduledTaskType,
    KeyedScheduledTaskTypeType,
    TransitionOptionsType,
    ScheduledTaskTypeEnum
} from './types';

import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';

import {AsyncPicker} from '../common/ak/AsyncPicker';
import {EditorField} from '../common/ak/EditorField';
import {JqlInput} from '../common/ak/JqlInput';
import {FieldError} from '../common/ak/FieldError';
import {ErrorMessage} from '../common/ak/messages';

import {scheduledTaskService} from '../service/services';
import {getMarkers} from '../common/error';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {Bindings, ReturnTypes} from '../common/bindings';
import {addItem, updateItem} from '../common/redux';
import type {FullDialogComponentProps} from '../common/script-list/types';
import type {InputEvent} from '../common/EventTypes';
import type {BindingType} from '../common/editor/types';
import type {SingleValueType} from '../common/ak/types';


const issueBindings = [Bindings.issue];
const emptyBindings = [];
const returnTypes = [ReturnTypes.void];

function getValue(option: any): ?string {
    return option ? option.value : null;
}

type Form = {
    name: string,
    type: ?ScheduledTaskTypeEnum,
    scheduleExpression: string,
    transitionOptions: TransitionOptionsType,
    issueWorkflowName: ?SingleValueType,
    issueWorkflowActionId: ?SingleValueType,
    userKey: ?SingleValueType,
    description: ?string,
    issueJql: ?string,
    scriptBody: ?string,
    comment: ?string,
};

type FormField = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    name: '',
    type: null,
    scheduleExpression: '',
    transitionOptions: {},
    issueWorkflowName: null,
    issueWorkflowActionId: null,
    userKey: null,
    scriptBody: null,
    issueJql: null,
    comment: null,
    description: null
});

type Props = FullDialogComponentProps & {
    updateItem: typeof updateItem,
    addItem: typeof addItem
};

type State = {
    ready: boolean,
    values: RecordOf<Form>,
    task: ?ScheduledTaskType,
    error: *
};

export class ScheduledTaskDialogInternal extends React.PureComponent<Props, State> {
    state = {
        ready: false,
        values: makeForm(),
        task: null,
        error: null
    };

    componentWillReceiveProps(nextProps: Props) {
        this._init(nextProps);
    }

    componentDidMount() {
        this._init(this.props);
    }

    _init = ({isNew, id}: Props) => {
        if (!isNew && id) {
            this.setState({
                ready: false,
                values: makeForm(),
                error: null
            });

            scheduledTaskService
                .get(id)
                .then((task: ScheduledTaskType) => {
                    this.setState({
                        values: makeForm({
                            name: task.name,
                            description: task.description,
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
                        ready: true,
                        task
                    });
                });
        } else {
            this.setState({
                ready: true,
                values: makeForm({
                    name: '',
                    description: '',
                    transitionOptions: {},
                    scriptBody: ''
                }),
                error: null,
                task: null
            });
        }
    };

    _handleError = (error: *) => {
        const {response} = error;

        if (response.status === 400) {
            this.setState({error: response.data});
        } else {
            throw error;
        }
    };

    _onSubmit = () => {
        const {isNew, id, onClose, updateItem, addItem} = this.props;

        const jsData = this.state.values.toJS();
        const data = {
            ...jsData,
            userKey: getValue(jsData.userKey),
            issueWorkflowName: getValue(jsData.issueWorkflowName),
            issueWorkflowActionId: getValue(jsData.issueWorkflowActionId)
        };

        if (!isNew && id) {
            scheduledTaskService
                .update(id, data)
                .then(
                    (script: ScheduledTaskType) => {
                        onClose();
                        updateItem(script);
                    },
                    this._handleError
                );
        } else {
            scheduledTaskService
                .create(data)
                .then(
                    (script: ScheduledTaskType) => {
                        onClose();
                        addItem(script);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: FormField, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setTextValue = (field: FormField) => (event: InputEvent) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: FormField) => (value: any) => this.mutateValue(field, value);

    _toggleTransitionOption2 = (e: SyntheticEvent<HTMLInputElement>) => {
        const option = e.currentTarget.value;

        if (option) {
            this.setState((state: State): * => {
                const options = state.values.get('transitionOptions') || {};

                return {
                    values: state.values.set('transitionOptions', {
                        ...options,
                        [option]: !options[option]
                    })
                };
            });
        }
    };

    _renderField = (fieldName: (FormField | 'workflowAction'), e: *): Node => {
        const {values} = this.state;
        let error: * = e;

        let errorMessage: * = null;
        let errorField: ?string = null;

        if (error) {
            errorMessage = error.message || error.messages;
            errorField = error.field;
        }

        switch (fieldName) {
            case 'scriptBody': {
                let markers: * = null;

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

                let bindings: ?Array<BindingType> = null;

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
                        returnTypes={returnTypes}

                        value={values.get(fieldName) || ''}
                        onChange={this._setObjectValue(fieldName)}
                    />
                );
            }
            case 'issueJql': {
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
                            <WarningIcon size="small" label="" primaryColor={colors.Y300}/>
                            {' '}
                            {ScheduledTaskMessages.jqlLimitDescription('1000') /*todo: insert value from config when its configurable*/}
                        </div>
                        {['ISSUE_JQL_SCRIPT', 'DOCUMENT_ISSUE_JQL_SCRIPT'].includes(values.get('type')) &&
                        <div className="ak-description">
                            <WarningIcon size="small" label="" primaryColor={colors.Y300}/>
                            {' '}
                            {ScheduledTaskMessages.jqlScriptDescription}
                        </div>
                        }
                        {errorField === fieldName && <FieldError error={errorMessage}/>}
                    </div>
                );
            }
            case 'workflowAction': {
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
            }
            case 'transitionOptions': {
                const value = ((values.get(fieldName) || {}): TransitionOptionsType);
                //todo: specific flow type
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
            }
            default:
                return <div key={fieldName}>{'NOT IMPLEMENTED'}</div>;
        }
    };

    render() {
        const {onClose, isNew} = this.props;
        const {ready, values, task, error} = this.state;

        let body: Node = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            const currentType = values.get('type');

            let errorMessage: * = null;
            let errorField: ?string = null;

            if (error) {
                errorMessage = error.message || error.messages;
                errorField = error.field;
            }

            body =
                <div className="flex-column">
                    {error && !errorField && <ErrorMessage title={errorMessage}/>}

                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={values.get('name') || ''}
                        onChange={this._setTextValue('name')}
                    />

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        minimumRows={5}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorField === 'description' ? errorMessage : null}

                        label={FieldMessages.description}
                        value={values.get('description') || ''}
                        onChange={this._setTextValue('description')}
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
                    <div className="ak-description">
                        {ScheduledTaskMessages.scheduleDescription}
                    </div>

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

                        items={typeList.map((type: KeyedScheduledTaskTypeType): ItemPropType => {
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

            isHeadingMultiline={false}
            heading={isNew ? ScheduledTaskMessages.addTask : `${ScheduledTaskMessages.editTask}: ${task ? task.name : ''}`}

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

export const ScheduledTaskDialog = connect(() => ({}), { updateItem, addItem })(ScheduledTaskDialogInternal);
