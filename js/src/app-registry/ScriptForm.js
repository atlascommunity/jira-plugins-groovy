//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';
import {Link, Prompt, withRouter} from 'react-router-dom';

import Button, {ButtonGroup} from '@atlaskit/button';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';
import {Label} from '@atlaskit/field-base';
import {CheckboxStateless} from '@atlaskit/checkbox';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {Record, type RecordOf, type RecordFactory} from 'immutable';

import {RegistryActionCreators} from './registry.reducer';

import type {WorkflowScriptType, RegistryScriptType} from './types';

import {FieldMessages, CommonMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';

import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {StaticField} from '../common/ak/StaticField';
import {FieldError} from '../common/ak/FieldError';
import {ErrorMessage} from '../common/ak/messages';
import {RegistryMessages} from '../i18n/registry.i18n';
import type {InputEvent} from '../common/EventTypes';

import './ScriptForm.less';


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
        script: null
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
                    error: null,
                    waiting: false
                }));
        } else if (directoryId) {
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
                        error: null,
                        waiting: false
                    })
                );
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
                        this.setState({ waiting: false });
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
                        this.setState({ waiting: false });
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

    render(): Node {
        const {values, script, parentName, error, modified, fetching, waiting} = this.state;
        let errorMessage: * = null;
        let errorField: ?string = null;

        let markers: * = null;
        if (error) {
            if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                const errors = error.error.filter(e => e);
                if (!modified) {
                    markers = getMarkers(errors);
                }
                errorMessage = errors
                    .map(error => error.message)
                    .map(error => <p key={error}>{error}</p>);
            } else {
                errorMessage = error.message;
            }
            errorField = error.field;
        }

        const types = values.get('types') || [];

        return (
            <Page>
                <PageHeader>
                    {this.state.id ? `${RegistryMessages.editScript}: ${script ? script.name : ''}` : RegistryMessages.addScript}
                </PageHeader>
                <Prompt when={modified && !waiting} message="Are you sure you want to leave?"/>
                {fetching && <div className="flex-horizontal-middle"><div className="flex-vertical-middle"><Spinner size="medium"/></div></div>}
                {!fetching && <div className="ScriptForm">
                    {error && !errorField && <ErrorMessage title={errorMessage}/>}
                    <StaticField label={FieldMessages.parentName}>
                        {parentName}
                    </StaticField>

                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}
                        maxLength={64}

                        disabled={waiting}
                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={values.get('name') || ''}
                        onChange={this._setTextValue('name')}
                    />

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        minimumRows={5}

                        disabled={waiting}
                        isInvalid={errorField === 'description'}
                        invalidMessage={errorField === 'description' ? errorMessage : null}

                        label={FieldMessages.description}
                        value={values.get('description') || ''}
                        onChange={this._setTextValue('description')}
                    />

                    <div>
                        <Label isRequired={true} label={FieldMessages.type}/>
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
                        {errorField === 'types' && <FieldError error={errorMessage}/>}
                    </div>

                    <EditorField
                        label={FieldMessages.scriptCode}
                        isRequired={true}
                        resizable={true}

                        isDisabled={waiting}
                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                        markers={markers}

                        bindings={bindings}

                        value={values.get('scriptBody') || ''}
                        onChange={this._setScript}
                    />

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        required={!!this.state.id}

                        disabled={waiting}
                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorField === 'comment' ? errorMessage : null}

                        label={FieldMessages.comment}
                        value={values.get('comment') || ''}
                        onChange={this._setTextValue('comment')}
                    />

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
