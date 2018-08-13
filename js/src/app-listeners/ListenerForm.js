//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';
import {withRouter, Prompt} from 'react-router-dom';

import {Record} from 'immutable';
import type {RecordOf, RecordFactory} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import {ConditionPicker} from './ConditionPicker';

import type {ListenerType, ConditionInputType} from './types';

import {ListenerMessages} from '../i18n/listener.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {RouterLink, FormField, CheckedEditorField, ErrorMessage} from '../common/ak';

import {listenerService} from '../service/services';
import {getMarkers} from '../common/error';
import {Bindings, ReturnTypes} from '../common/bindings';
import {addItem, updateItem} from '../common/redux';
import type {DialogComponentProps} from '../common/script-list/types';
import type {BindingType} from '../common/editor/types';
import {extractShortClassName} from '../common/classNames';
import {withRoot} from '../common/script-list/breadcrumbs';
import {ScrollToTop} from '../common/ScrollToTop';


//AbstractProjectEvent
//ProjectCategoryChangeEvent
//AbstractVersionEvent
//AbstractProjectComponentEvent
//DirectoryEvent
//AbstractCustomFieldEvent
//AbstractWorklogEvent
//IndexEvent
//AbstractRemoteIssueLinkEvent
//IssueWatcherAddedEvent
//IssueWatcherDeletedEvent

const issueEventBindings = [Bindings.issueEvent];
const returnTypes = [ReturnTypes.void];

type Form = {
    name: string,
    description: string,
    comment: string,
    scriptBody: string,
    condition: ConditionInputType
};

type FormFieldKey = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    name: '',
    description: '',
    comment: '',
    scriptBody: '',
    condition: {
        type: null,
        typeIds: [],
        projectIds: [],
        className: null
    }
});

type Props = DialogComponentProps & {
    addItem: typeof addItem,
    updateItem: typeof updateItem,
    history: any
};

type State = {
    ready: boolean,
    waiting: boolean,
    isModified: boolean,
    values: RecordOf<Form>,
    listener: ?ListenerType,
    error: *
};

class ListenerFormInternal extends React.PureComponent<Props, State> {
    state = {
        ready: false,
        waiting: false,
        isModified: false,
        values: makeForm(),
        listener: null,
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
                waiting: false,
                ready: false,
                values: makeForm()
            });

            listenerService
                .getListener(id)
                .then((listener: ListenerType) => {
                    this.setState({
                        values: makeForm({
                            name: listener.name,
                            description: listener.description || '',
                            scriptBody: listener.scriptBody,
                            //$FlowFixMe todo
                            condition: listener.condition
                        }),
                        listener: listener,
                        ready: true,
                        isModified: false
                    });
                });
        } else {
            this.setState({
                ready: true,
                isModified: false,
                values: makeForm(),
                listener: null
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
        const {isNew, id, addItem, updateItem, history} = this.props;
        const data = this.state.values.toJS();

        this.setState({waiting: true});

        if (!isNew && id) {
            listenerService
                .updateListener(id, data)
                .then(
                    (listener: ListenerType) => {
                        history.push('/listeners/');
                        updateItem(listener);
                    },
                    this._handleError
                );
        } else {
            listenerService
                .createListener(data)
                .then(
                    (listener: ListenerType) => {
                        history.push('/listeners/');
                        addItem(listener);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: FormFieldKey, value: any) =>
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value),
                isModified: true
            };
        });

    _setTextValue = (field) => (event) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    render() {
        const {isNew} = this.props;
        const {ready, waiting, isModified, values, listener, error} = this.state;

        let body: ?Node = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            let errorMessage: * = null;
            let errorField: ?string = null;

            let markers: * = null;

            if (error) {
                if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                    const errors = error.error.filter(e => e);
                    markers = getMarkers(errors);
                    errorMessage = errors
                        .map(error => error.message)
                        .map(error => <p key={error}>{error}</p>);
                } else {
                    errorMessage = error.message;
                }
                errorField = error.field;
            }

            let bindings: ?Array<BindingType> = null;

            const condition = values.get('condition') || {};
            if (condition && condition.type) {
                if (condition.type === 'ISSUE') {
                    bindings = issueEventBindings;
                } else {
                    const className = condition.className;
                    if (className) {
                        const extractedName = extractShortClassName(className);
                        if (extractedName) {
                            bindings = [{
                                ...Bindings.event,
                                className: extractedName,
                                fullClassName: className
                            }];
                        }
                    }
                }
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

                    <ConditionPicker
                        value={condition}
                        onChange={this._setObjectValue('condition')}

                        error={error}
                        isDisabled={waiting}
                    />

                    <FormField
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                    >
                        <CheckedEditorField
                            isDisabled={waiting}

                            scriptType="LISTENER"
                            typeParams={{
                                //$FlowFixMe
                                className: (condition.type === 'ISSUE' ? 'com.atlassian.jira.event.issue.IssueEvent' : condition.className || undefined)
                            }}

                            bindings={bindings || undefined}
                            returnTypes={returnTypes}

                            value={values.get('scriptBody') || ''}
                            onChange={this._setObjectValue('scriptBody')}
                        />
                    </FormField>
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
                                href="/listeners/"
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
                                    key="fields"
                                    text="Listeners"
                                    href="/listeners"
                                    component={RouterLink}
                                />,
                                !isNew && listener ? <BreadcrumbsItem
                                    key="script"
                                    text={listener.name}
                                    href={`/listeners/${listener.id}/view`}
                                    component={RouterLink}
                                /> : null
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {isNew ?
                        ListenerMessages.addListener :
                        `${ListenerMessages.editListener}: ${listener ? listener.name : ''}`
                    }
                </PageHeader>
                <ScrollToTop/>
                <Prompt when={isModified && !waiting} message="Are you sure you want to leave?"/>

                {body}
            </Page>
        );
    }
}

export const ListenerForm = withRouter(
    connect(
        null,
        { addItem, updateItem }
    )(ListenerFormInternal)
);
