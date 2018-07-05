//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import {Record, type RecordOf, type RecordFactory} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import type {RestScriptType} from './types';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {MultiSelect, AsyncPicker, EditorField, FormField, ErrorMessage, RouterLink} from '../common/ak';
import {withRoot} from '../common/script-list';

import {restService} from '../service/services';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {RegistryMessages} from '../i18n/registry.i18n';
import {addItem, updateItem} from '../common/redux';
import type {DialogComponentProps} from '../common/script-list/types';
import type {HttpMethod} from '../common/types';
import type {OldSelectItem, SingleValueType} from '../common/ak/types';
import type {InputEvent} from '../common/EventTypes';


const returnTypes = [
    {
        label: 'custom', //todo: i18n
        className: 'Response',
        fullClassName: 'javax.ws.rs.core.Response',
        javaDoc: 'https://docs.oracle.com/javaee/7/api/javax/ws/rs/core/Response.html',
        optional: true
    },
    {
        label: 'No content (204)', //todo: i18n
        className: 'null',
        fullClassName: 'null'
    }
];

const httpMethods = ['GET', 'POST', 'PUT', 'DELETE'].map(
    (method: HttpMethod): OldSelectItem<string> => {
        return {
            label: method,
            value: method
        };
    }
);

const bindings = [ Bindings.method, Bindings.headers, Bindings.uriInfo, Bindings.body, Bindings.currentUser ];

type Form = {
    name: string,
    description: string,
    comment: string,
    scriptBody: string,
    methods: $ReadOnlyArray<HttpMethod>,
    groups: $ReadOnlyArray<SingleValueType>
};

type FormFieldKey = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    name: '',
    description: '',
    comment: '',
    scriptBody: '',
    methods: [],
    groups: []
});

type Props = DialogComponentProps & {
    updateItem: typeof updateItem,
    addItem: typeof addItem,
    history: any
};

type State = {
    ready: boolean,
    waiting: boolean,
    values: RecordOf<Form>,
    error: *,
    script: ?RestScriptType
};

export class RestFormInternal extends React.Component<Props, State> {
    state = {
        ready: false,
        waiting: false,
        values: makeForm(),
        error: null,
        script: null
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

            restService
                .getScript(id)
                .then((script: RestScriptType) => {
                    this.setState({
                        values: makeForm({
                            name: script.name,
                            methods: script.methods,
                            scriptBody: script.scriptBody,
                            groups: script.groups.map((group: string): SingleValueType => {
                                return {
                                    label: group,
                                    value: group
                                };
                            }),
                            description: script.description || '',
                            comment: ''
                        }),
                        ready: true,
                        script
                    });
                });
        } else {
            this.setState({
                ready: true,
                values: makeForm({
                    name: '',
                    methods: [],
                    groups: [],
                    scriptBody: ''
                }),
                error: null,
                script: null
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
        const {isNew, id, history} = this.props;

        const {groups, ...data}: any = this.state.values.toJS();
        data.groups = groups ? groups.map(group => group.value) : [];

        if (!isNew && id) {
            restService
                .updateScript(id, data)
                .then(
                    (script: RestScriptType) => {
                        history.push('/rest/');
                        this.props.updateItem(script);
                    },
                    this._handleError
                );
        } else {
            restService
                .createScript(data)
                .then(
                    (script: RestScriptType) => {
                        history.push('/rest/');
                        this.props.addItem(script);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: FormFieldKey, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setTextValue = (field: FormFieldKey) => (event: InputEvent) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: FormFieldKey) => (value: any) => this.mutateValue(field, value);

    render() {
        const {isNew} = this.props;
        const {ready, waiting, values, script, error} = this.state;

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

            body = (
                <div className="flex-column">
                    {error && !errorField && <ErrorMessage title={errorMessage}/>}

                    <FormField
                        label={FieldMessages.name}
                        isRequired={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        helperText={RestMessages.nameDescription}
                    >
                        <FieldTextStateless
                            shouldFitContainer={true}

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

                            value={values.get('description') || ''}
                            onChange={this._setTextValue('description')}
                        />
                    </FormField>

                    <MultiSelect
                        label={FieldMessages.httpMethods}
                        isRequired={true}

                        isInvalid={errorField === 'methods'}
                        invalidMessage={errorField === 'methods' ? <div className="error">{errorMessage}</div> : ''}

                        items={httpMethods}
                        value={values.get('methods')}
                        onChange={this._setObjectValue('methods')}
                    />
                    <AsyncPicker
                        label={FieldMessages.groups}
                        isMulti={true}

                        src={`${getPluginBaseUrl()}/jira-api/groupPicker`}

                        value={values.get('groups')}
                        onChange={this._setObjectValue('groups')}

                        isInvalid={errorField === 'groups'}
                        invalidMessage={errorField === 'groups' ? errorMessage : ''}
                    />

                    <FormField
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                    >
                        <EditorField
                            markers={markers}
                            bindings={bindings}
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
                                href="/rest/"
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
                                    text="REST scripts"
                                    href="/rest"
                                    component={RouterLink}
                                />,
                                !isNew && script ? <BreadcrumbsItem
                                    key="script"
                                    text={script.name}
                                    href={`/rest/${script.id}/view`}
                                    component={RouterLink}
                                /> : null
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {isNew ?
                        RegistryMessages.addScript:
                        `${RegistryMessages.editScript}: ${script ? script.name : ''}`
                    }
                </PageHeader>
                {body}
            </Page>
        );
    }
}

export const RestForm = withRouter(
    connect(
        null,
        { addItem, updateItem }
    )(RestFormInternal)
);

