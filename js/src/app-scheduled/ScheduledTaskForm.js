//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';
import {Prompt, withRouter, type RouterHistory} from 'react-router-dom';

import {Record, type RecordOf, type RecordFactory} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {AkFieldRadioGroup} from '@atlaskit/field-radio-group';
import {Checkbox} from '@atlaskit/checkbox';
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

import {AsyncPicker, CheckedEditorField, JqlInput, FormField, FieldError, ErrorMessage, RouterLink} from '../common/ak';
import {ScrollToTop} from '../common/ScrollToTop';
import {withRoot} from '../common/script-list';

import {scheduledTaskService, getPluginBaseUrl} from '../service';
import {Bindings, ReturnTypes} from '../common/bindings';
import {addItem, updateItem} from '../common/redux';

import type {DialogComponentProps} from '../common/script-list/types';
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

type FormFieldKey = $Keys<Form>;

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

type Props = {|
    ...DialogComponentProps,
    updateItem: typeof updateItem,
    addItem: typeof addItem,
    history: RouterHistory
|};

type State = {
    ready: boolean,
    waiting: boolean,
    isModified: boolean,
    values: RecordOf<Form>,
    task: ?ScheduledTaskType,
    error: *
};

export class ScheduledTaskFormInternal extends React.PureComponent<Props, State> {
    state = {
        ready: false,
        waiting: false,
        isModified: false,
        values: makeForm(),
        task: null,
        error: null
    };

    componentDidUpdate(prevProps: Props) {
        const {isNew, id} = this.props;

        if (prevProps.isNew !== isNew || prevProps.id !== id) {
            this._init(this.props);
        }
    }

    componentDidMount() {
        this._init(this.props);
    }

    _init = ({isNew, id}: Props) => {
        if (!isNew && id) {
            this.setState({
                ready: false,
                waiting: false,
                isModified: false,
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
                        isModified: false,
                        task
                    });
                });
        } else {
            this.setState({
                ready: true,
                isModified: false,
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
            this.setState({ error: response.data, waiting: false });
        } else {
            this.setState({ waiting: false });
            throw error;
        }
    };

    _onSubmit = () => {
        const {isNew, id, updateItem, addItem, history} = this.props;

        this.setState({ waiting: true });

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
                        history.push('/scheduled', {focus: script.id});
                        updateItem(script);
                    },
                    this._handleError
                );
        } else {
            scheduledTaskService
                .create(data)
                .then(
                    (script: ScheduledTaskType) => {
                        history.push('/scheduled', {focus: script.id});
                        addItem(script);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: FormFieldKey, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value),
                isModified: true
            };
        });
    };

    _setTextValue = (field: FormFieldKey) => (event: InputEvent) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: FormFieldKey) => (value: any) => this.mutateValue(field, value);

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

    _renderField = (fieldName: (FormFieldKey | 'workflowAction'), e: *): Node => {
        const {values, waiting} = this.state;
        let error: * = e;

        let errorMessage: * = null;
        let errorField: ?string = null;

        if (error) {
            errorMessage = error.message || error.messages;
            errorField = error.field;
        }

        switch (fieldName) {
            case 'scriptBody': {
                if (error && error.field === fieldName) {
                    if (Array.isArray(error.error)) {
                        const errors = error.error.filter(e => e);
                        error = {
                            field: fieldName,
                            messages: errors.map(error => error.message)
                        };
                    }
                }

                const currentType = values.get('type');

                let bindings: ?Array<BindingType> = null;

                let withIssue: boolean = false;
                let isMutableIssue: boolean = currentType === 'ISSUE_JQL_SCRIPT';

                switch (currentType) {
                    case 'ISSUE_JQL_SCRIPT':
                    case 'DOCUMENT_ISSUE_JQL_SCRIPT':
                        bindings = issueBindings;
                        withIssue = true;
                        break;
                    default:
                        bindings = emptyBindings;
                }

                return (
                    <CheckedEditorField
                        key={fieldName}

                        label={FieldMessages.scriptCode}
                        isRequired={true}
                        isDisabled={waiting}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}

                        scriptType="SCHEDULED_TASK"
                        typeParams={{
                            withIssue: withIssue ? 'true' : 'false',
                            isMutableIssue: isMutableIssue ? 'true' : 'false'
                        }}
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
                            isDisabled={waiting}

                            isInvalid={errorField === fieldName}
                            invalidMessage={errorField === fieldName ? errorMessage : ''}

                            shouldFitContainer={true}

                            value={values.get(fieldName) || ''}
                            onChange={this._setTextValue(fieldName)}
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
                            isDisabled={waiting}

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
                            isDisabled={waiting}

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
                        <div>
                            <Checkbox
                                isChecked={value.skipConditions || false}
                                isDisabled={waiting}
                                onChange={this._toggleTransitionOption2}
                                label={ScheduledTaskMessages.transitionOption.skipConditions}
                                value="skipConditions"
                                name="transition-options"
                            />
                            <Checkbox
                                isChecked={value.skipValidators || false}
                                isDisabled={waiting}
                                onChange={this._toggleTransitionOption2}
                                label={ScheduledTaskMessages.transitionOption.skipValidators}
                                value="skipValidators"
                                name="transition-options"
                            />
                            <Checkbox
                                isChecked={value.skipPermissions || false}
                                isDisabled={waiting}
                                onChange={this._toggleTransitionOption2}
                                label={ScheduledTaskMessages.transitionOption.skipPermissions}
                                value="skipPermissions"
                                name="transition-options"
                            />
                        </div>
                    </div>
                );
            }
            default:
                return <div key={fieldName}>{'NOT IMPLEMENTED'}</div>;
        }
    };

    render() {
        const {isNew} = this.props;
        const {ready, waiting, isModified, values, task, error} = this.state;

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

            body = (
                <div className="flex-column">
                    {error && !errorField && <ErrorMessage title={errorMessage || undefined}/>}

                    <FormField
                        label={FieldMessages.name}
                        isRequired={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}
                    >
                        <FieldTextStateless
                            shouldFitContainer={true}
                            disabled={waiting}

                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                    </FormField>

                    <FormField
                        label={FieldMessages.description}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorField === 'description' ? errorMessage : null}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            minimumRows={5}
                            disabled={waiting}

                            value={values.get('description') || ''}
                            onChange={this._setTextValue('description')}
                        />
                    </FormField>

                    <FormField
                        label={FieldMessages.schedule}
                        isRequired={true}

                        isInvalid={errorField === 'scheduleExpression'}
                        invalidMessage={errorField === 'scheduleExpression' ? errorMessage : null}

                        helperText={ScheduledTaskMessages.scheduleDescription}
                    >
                        <FieldTextStateless
                            shouldFitContainer={true}
                            required={true}
                            disabled={waiting}

                            value={values.get('scheduleExpression') || ''}
                            onChange={this._setTextValue('scheduleExpression')}
                        />
                    </FormField>

                    <AsyncPicker
                        label={ScheduledTaskMessages.runAs}
                        isRequired={true}
                        isDisabled={waiting}

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
                                isSelected: values.get('type') === type.key,
                                isDisabled: waiting
                            };
                        })}
                        value={values.get('type')}
                        onRadioChange={this._setTextValue('type')}
                    />
                    {errorField === 'type' && <FieldError error={errorMessage}/>}

                    {currentType && types[currentType].fields.map(field => this._renderField(field, error))}

                    <FormField
                        label={FieldMessages.comment}
                        isRequired={!isNew}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorField === 'comment' ? errorMessage : null}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            disabled={waiting}

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    </FormField>

                    <div style={{marginTop: '10px'}}>
                        <ButtonGroup>
                            <Button
                                appearance="primary"
                                isLoading={waiting}

                                onClick={this._onSubmit}
                            >
                                {isNew ? CommonMessages.create : CommonMessages.update}
                            </Button>
                            <Button
                                appearance="link"

                                isDisabled={waiting}

                                component={RouterLink}
                                href="/scheduled/"
                            >
                                {CommonMessages.cancel}
                            </Button>
                        </ButtonGroup>
                    </div>
                </div>
            );
        }

        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="parent"
                                    text="Scheduled tasks"
                                    href="/scheduled"
                                    component={RouterLink}
                                />,
                                !isNew && task
                                    ? (
                                        <BreadcrumbsItem
                                            key="task"
                                            text={task.name}
                                            href={`/scheduled/${task.id}/view`}
                                            component={RouterLink}
                                        />
                                    )
                                    : null
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {isNew ? ScheduledTaskMessages.addTask : `${ScheduledTaskMessages.editTask}: ${task ? task.name : ''}`}
                </PageHeader>
                <ScrollToTop/>
                <Prompt when={isModified && !waiting} message="Are you sure you want to leave?"/>
                {body}
            </Page>
        );
    }
}

export const ScheduledTaskForm = withRouter(connect(() => ({}), { updateItem, addItem })(ScheduledTaskFormInternal));
