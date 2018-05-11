//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';
import {Label} from '@atlaskit/field-base';
import {CheckboxStateless} from '@atlaskit/checkbox';

import {Map, type Map as MapType} from 'immutable';

import {RegistryActionCreators} from './registry.reducer';

import type {RegistryScriptType} from './types';

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


const bindings = [ Bindings.mutableIssue, Bindings.currentUser, Bindings.transientVars ];

type Props = {
    addScript: typeof RegistryActionCreators.addScript,
    updateScript: typeof RegistryActionCreators.updateScript
};

type State = {
    active: boolean,
    fetching: boolean,
    id: ?number,
    values: MapType<string, any>,
    parentName: string,
    error: *,
    modified: boolean,
    waiting: boolean,
    script: ?RegistryScriptType
};

//todo: use declarative activation
export class ScriptDialogInternal extends React.PureComponent<Props, State> {
    state = {
        active: false,
        fetching: false,
        id: null,
        values: new Map(),
        parentName: '',
        error: null,
        modified: false,
        waiting: false,
        script: null
    };

    activateCreate = (directoryId: number) => {
        this.setState({ active: true, fetching: true });

        registryService
            .getDirectory(directoryId)
            .then(directory =>
                this.setState({
                    fetching: false,
                    active: true,
                    id: null,
                    values: Map({
                        directoryId: directoryId,
                        types: []
                    }),
                    parentName: directory.fullName,
                    error: null,
                    waiting: false
                })
            );
    };

    activateEdit = (id: number) => {
        this.setState({ active: true, fetching: true });

        registryService
            .getScript(id)
            .then(data => this.setState({
                fetching: false,
                active: true,
                id: id,
                values: Map({
                    name: data.name,
                    description: data.description,
                    types: data.types,
                    scriptBody: data.scriptBody,
                    directoryId: data.directoryId
                }),
                script: data,
                parentName: data.parentName,
                error: null,
                waiting: false
            }));
    };

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
        this.setState({ waiting: true });

        const id = this.state.id;
        if (id) {
            registryService
                .updateScript(id, this.state.values.toJS())
                .then(
                    (data: RegistryScriptType) => {
                        this.props.updateScript(data);
                        this.setState({ active: false, waiting: false });
                    },
                    this._handleError
                );
        } else {
            registryService
                .createScript(this.state.values.toJS())
                .then(
                    (data: RegistryScriptType) => {
                        this.props.addScript(data);
                        this.setState({ active: false, waiting: false });
                    },
                    this._handleError
                );
        }
    };

    _close = () => this.setState({ active: false, waiting: false, values: Map() });

    mutateValue = (field: string, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value),
                modified: true
            };
        });
    };

    _setTextValue = (field: string) => (event: InputEvent) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: string) => (value: any) => this.mutateValue(field, value);

    _setScript = this._setObjectValue('scriptBody');

    _toggleType = (e: SyntheticEvent<HTMLInputElement>) => {
        const option = e.currentTarget.value;

        this.setState((state: State): * => {
            const types = state.values.get('types') || [];

            const isRemove = types.includes(option);

            return {
                values: state.values.set('types', isRemove ? types.filter(type => type !== option) : [...types, option])
            };
        });
    };

    render(): Node {
        const {values, script, parentName, error, modified, active, fetching, waiting} = this.state;
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
            <div>
                {active ?
                    <ModalDialog
                        width="x-large"
                        scrollBehavior="outside"

                        isHeadingMultiline={false}
                        heading={this.state.id ? `${RegistryMessages.editScript}: ${script ? script.name : ''}` : RegistryMessages.addScript}

                        onClose={this._close}

                        actions={[
                            {
                                text: this.state.id ? CommonMessages.update : CommonMessages.create,
                                onClick: this._onSubmit,
                                isDisabled: waiting || fetching
                            },
                            {
                                text: CommonMessages.cancel,
                                onClick: this._close,
                                isDisabled: waiting || fetching
                            }
                        ]}
                    >
                        {fetching && <div className="flex-horizontal-middle"><div className="flex-vertical-middle"><Spinner size="medium"/></div></div>}
                        {!fetching && <div className="flex-column">
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
                        </div>}
                    </ModalDialog>
                    : null}
            </div>
        );
    }
}

export const ScriptDialog = connect(
    null,
    //$FlowFixMe
    RegistryActionCreators,
    null,
    {withRef: true}
)(ScriptDialogInternal);
