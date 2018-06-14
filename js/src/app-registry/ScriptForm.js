//@flow
import React from 'react';

import {connect} from 'react-redux';
import {Link, Prompt, withRouter} from 'react-router-dom';

import Button, {ButtonGroup} from '@atlaskit/button';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';
import {CheckboxStateless, CheckboxGroup} from '@atlaskit/checkbox';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import {Field} from '@atlaskit/form';

import {Record, type RecordOf, type RecordFactory} from 'immutable';

import {RegistryActionCreators} from './redux/actions';

import type {WorkflowScriptType, RegistryScriptType} from './types';

import {FieldMessages, CommonMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';

import {getMarkers} from '../common/error';
import {Bindings, ReturnTypes} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {StaticField} from '../common/ak/StaticField';
import {ErrorMessage} from '../common/ak/messages';
import {AsyncPicker} from '../common/ak/AsyncPicker';
import {RegistryMessages} from '../i18n/registry.i18n';
import {getPluginBaseUrl} from '../service/ajaxHelper';

import type {InputEvent} from '../common/EventTypes';
import type {SingleValueType} from '../common/ak/types';

import './ScriptForm.less';


const returnTypesMap = {
    CONDITION: {
        ...ReturnTypes.boolean,
        label: CommonMessages.condition
    },
    FUNCTION: {
        ...ReturnTypes.void,
        label: CommonMessages.function
    },
    VALIDATOR: {
        ...ReturnTypes.void,
        label: CommonMessages.validator
    }
};

const bindings = [ Bindings.mutableIssue, Bindings.currentUser, Bindings.transientVars ];

type Form = {
    directoryId: number,
    types: $ReadOnlyArray<WorkflowScriptType>,
    name: string,
    description: string,
    comment: string,
    scriptBody: string
};

type FormField = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    directoryId: 0,
    name: '',
    types: [],
    description: '',
    comment: '',
    scriptBody: '',
});

type Props = {
    addScript: typeof RegistryActionCreators.addScript,
    updateScript: typeof RegistryActionCreators.updateScript,
    history: any,
    isNew: boolean,
    id: ?number,
    directoryId: ?number,
};

type State = {
    fetching: boolean,
    id: ?number,
    values: RecordOf<Form>,
    parentName: string,
    noParent: boolean,
    error: *,
    modified: boolean,
    waiting: boolean,
    script: ?RegistryScriptType
};

export class ScriptFormInternal extends React.PureComponent<Props, State> {
    state = {
        fetching: false,
        id: null,
        values: makeForm(),
        parentName: '',
        error: null,
        modified: false,
        waiting: false,
        script: null,
        noParent: false
    };

    componentDidMount() {
        const {id, directoryId, isNew} = this.props;

        this.setState({ fetching: true });

        if (!isNew && id) {
            registryService
                .getScript(id)
                .then(data => this.setState({
                    fetching: false,
                    id: id,
                    values: makeForm({
                        name: data.name,
                        description: data.description || '',
                        types: data.types || [],
                        scriptBody: data.scriptBody,
                        directoryId: data.directoryId
                    }),
                    script: data,
                    parentName: data.parentName,
                    noParent: false,
                    error: null,
                    waiting: false
                }));
        } else if (directoryId) {
            if (directoryId === -1) {
                this.setState({
                    fetching: false,
                    id: null,
                    values: makeForm({
                        directoryId: -1,
                        types: []
                    }),
                    parentName: '',
                    noParent: true,
                    error: null,
                    waiting: false
                });
            } else {
                registryService
                    .getDirectory(directoryId)
                    .then(directory =>
                        this.setState({
                            fetching: false,
                            id: null,
                            values: makeForm({
                                directoryId: directoryId,
                                types: []
                            }),
                            parentName: directory.fullName,
                            noParent: false,
                            error: null,
                            waiting: false
                        })
                    );
            }
        }
    }

    _handleError = (error: *) => {
        const {response} = error;

        this.setState({ waiting: false });
        if (response.status === 400) {
            this.setState({
                error: response.data,
                modified: false
            });
        } else {
            throw error;
        }
    };

    _onSubmit = () => {
        const {history} = this.props;

        this.setState({ waiting: true });

        const id = this.state.id;
        if (id) {
            registryService
                .updateScript(id, this.state.values.toJS())
                .then(
                    (data: RegistryScriptType) => {
                        this.props.updateScript(data);
                        history.push('/');
                        //component should be unmounted at this point
                    },
                    this._handleError
                );
        } else {
            registryService
                .createScript(this.state.values.toJS())
                .then(
                    (data: RegistryScriptType) => {
                        this.props.addScript(data);
                        history.push('/');
                        //component should be unmounted at this point
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: FormField, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value),
                modified: true
            };
        });
    };

    _setTextValue = (field: FormField) => (event: InputEvent) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: FormField) => (value: any) => this.mutateValue(field, value);

    _setScript = this._setObjectValue('scriptBody');

    _toggleType = (e: SyntheticEvent<HTMLInputElement>) => {
        const option = e.currentTarget.value;

        this.setState((state: State): * => {
            const types = state.values.get('types') || [];

            const isRemove = types.includes(option);

            return {
                //$FlowFixMe todo
                values: state.values.set('types', isRemove ? types.filter(type => type !== option) : [...types, option])
            };
        });
    };

    _setDirectory = (value: ?SingleValueType) => {
        if (value) {
            const id = parseInt(value.value, 10);
            this.setState(({values}: *): * => {
                return {
                    values: values.set('directoryId', id),
                    parentName: value.label,
                    noParent: false
                };
            });
        }
    };

    render() {
        const {values, script, parentName, noParent, error, modified, fetching, waiting} = this.state;
        let errorMessage: ?string = null;
        let errorField: ?string = null;

        let markers: * = null;
        if (error) {
            if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                const errors = error.error.filter(e => e);
                if (!modified) {
                    markers = getMarkers(errors);
                }
                errorMessage = errors.map(error => error.message).join('; ');
            } else {
                errorMessage = error.message;
            }
            errorField = error.field;
        }

        const types = values.get('types') || [];

        const returnTypes = types.map(type => returnTypesMap[type]);

        return (
            <Page>
                <PageHeader>
                    {this.state.id ? `${RegistryMessages.editScript}: ${script ? script.name : ''}` : RegistryMessages.addScript}
                </PageHeader>
                <Prompt when={modified && !waiting} message="Are you sure you want to leave?"/>
                {fetching && <div className="flex-horizontal-middle"><div className="flex-vertical-middle"><Spinner size="medium"/></div></div>}
                {!fetching && <div className="ScriptForm">
                    {error && !errorField && <ErrorMessage title={errorMessage}/>}
                    {noParent ?
                        <Field
                            label={FieldMessages.parentName}
                            isRequired={true}

                            isInvalid={errorField === 'directoryId'}
                            invalidMessage={errorMessage || ''}

                            validateOnChange={false}
                            validateOnBlur={false}
                        >
                            <AsyncPicker
                                src={`${getPluginBaseUrl()}/registry/directory/picker`}

                                value={null}
                                onChange={this._setDirectory}

                                label=""
                            />
                        </Field>:
                        <Field label={FieldMessages.parentName}>
                            <StaticField label="">
                                {parentName}
                            </StaticField>
                        </Field>
                    }

                    <Field
                        label={FieldMessages.name}
                        isRequired={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <FieldTextStateless
                            shouldFitContainer={true}
                            maxLength={64}

                            disabled={waiting}

                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                    </Field>

                    <Field
                        label={FieldMessages.description}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            minimumRows={5}

                            disabled={waiting}

                            value={values.get('description') || ''}
                            onChange={this._setTextValue('description')}
                        />
                    </Field>

                    <Field
                        label={FieldMessages.type}
                        isRequired={true}

                        isInvalid={errorField === 'types'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <CheckboxGroup>
                            <CheckboxStateless
                                isChecked={types.includes('CONDITION')}
                                onChange={this._toggleType}
                                label={CommonMessages.condition}
                                value="CONDITION"
                                name="script-type-options"
                            />
                            <CheckboxStateless
                                isChecked={types.includes('VALIDATOR')}
                                onChange={this._toggleType}
                                label={CommonMessages.validator}
                                value="VALIDATOR"
                                name="script-type-options"
                            />
                            <CheckboxStateless
                                isChecked={types.includes('FUNCTION')}
                                onChange={this._toggleType}
                                label={CommonMessages.function}
                                value="FUNCTION"
                                name="script-type-options"
                            />
                        </CheckboxGroup>
                    </Field>

                    <Field
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <EditorField
                            label=""
                            resizable={true}

                            isDisabled={waiting}
                            markers={markers}

                            bindings={bindings}
                            returnTypes={returnTypes}

                            value={values.get('scriptBody') || ''}
                            onChange={this._setScript}
                        />
                    </Field>

                    <Field
                        label={FieldMessages.comment}
                        isRequired={!!this.state.id}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}

                            disabled={waiting}

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    </Field>

                    <div className="formButtons">
                        <ButtonGroup>
                            <Button
                                onClick={this._onSubmit}
                                appearance="primary"
                                isDisabled={waiting || fetching}
                                isLoading={waiting}
                            >
                                {this.state.id ? CommonMessages.update : CommonMessages.create}
                            </Button>
                            <Button component={Link} to="/" isDisabled={waiting || fetching} appearance="link">
                                {CommonMessages.cancel}
                            </Button>
                        </ButtonGroup>
                    </div>
                </div>}
            </Page>
        );
    }
}

export const ScriptForm = withRouter(
    connect(
        null,
        //$FlowFixMe
        RegistryActionCreators,
        null,
        {withRef: true}
    )(ScriptFormInternal)
);
