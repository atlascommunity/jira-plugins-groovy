import React from 'react';

import {connect} from 'react-redux';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';

import Message from 'aui-react/lib/AUIMessage';

import {Map} from 'immutable';

import {RegistryActionCreators} from './registry.reducer';

import {FieldMessages, CommonMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';

import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {StaticField} from '../common/ak/StaticField';


@connect(null, RegistryActionCreators, null, {withRef: true})
export class ScriptDialog extends React.Component {
    state = {
        active: false,
        fetching: false,
        id: null,
        values: new Map(),
        parentName: '',
        error: null,
        modified: false,
        waiting: false
    };

    activateCreate = (directoryId) => {
        this.setState({ active: true, fetching: true });

        registryService
            .getDirectory(directoryId)
            .then(directory =>
                this.setState({
                    fetching: false,
                    active: true,
                    id: null,
                    values: new Map({
                        directoryId: directoryId
                    }),
                    parentName: directory.fullName,
                    error: null,
                    waiting: false
                })
            );
    };

    activateEdit = (id) => {
        this.setState({ active: true, fetching: true });

        registryService
            .getScript(id)
            .then(data => this.setState({
                fetching: false,
                active: true,
                id: id,
                values: new Map({
                    name: data.name,
                    scriptBody: data.scriptBody,
                    directoryId: data.directoryId
                }),
                parentName: data.parentName,
                error: null,
                waiting: false
            }));
    };

    _handleError = (error) => {
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

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        this.setState({ waiting: true });

        const id = this.state.id;
        if (id) {
            registryService
                .updateScript(id, this.state.values.toJS())
                .then(
                    data => {
                        this.props.updateScript(data);
                        this.setState({ active: false, waiting: false });
                    },
                    this._handleError
                );
        } else {
            registryService
                .createScript(this.state.values.toJS())
                .then(
                    data => {
                        this.props.addScript(data);
                        this.setState({ active: false, waiting: false });
                    },
                    this._handleError
                );
        }
    };

    _close = () => this.setState({ active: false, waiting: false, values: Map() });

    mutateValue = (field, value) => {
        this.setState(state => {
            return {
                values: state.values.set(field, value),
                modified: true
            };
        });
    };

    _setTextValue = (field) => (event) => this.mutateValue(field, event.target.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    render() {
        const {values, parentName, error, modified, active, fetching, waiting} = this.state;
        let errorMessage = null;
        let errorField = null;

        let markers = null;
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

        console.log(error, markers);

        return (
            <div>
                {active ?
                    <ModalDialog
                        width="x-large"
                        scrollBehavior="outside"
                        heading={`${this.state.id ? CommonMessages.update : CommonMessages.create} script`}
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
                            {error && !errorField ?
                                <Message type="error">
                                    {errorMessage}
                                </Message>
                                : null}
                            <StaticField label={FieldMessages.parentName}>
                                {parentName}
                            </StaticField>
                            <FieldTextStateless
                                shouldFitContainer={true}
                                required={true}

                                disabled={waiting}
                                isInvalid={errorField === 'name'}
                                invalidMessage={errorField === 'name' ? errorMessage : null}

                                label={FieldMessages.name}
                                value={values.get('name') || ''}
                                onChange={this._setTextValue('name')}
                            />

                            <EditorField
                                label={FieldMessages.scriptCode}
                                isRequired={true}

                                isDisabled={waiting}
                                isInvalid={errorField === 'scriptBody'}
                                invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                                markers={markers}

                                bindings={[
                                    Bindings.mutableIssue, Bindings.currentUser, Bindings.transientVars
                                ]}

                                value={values.get('scriptBody') || ''}
                                onChange={this._setObjectValue('scriptBody')}
                            />

                            {!!this.state.id &&
                                <FieldTextAreaStateless
                                    shouldFitContainer={true}
                                    required={true}

                                    disabled={waiting}
                                    isInvalid={errorField === 'comment'}
                                    invalidMessage={errorField === 'comment' ? errorMessage : null}

                                    label={FieldMessages.comment}
                                    value={values.get('comment') || ''}
                                    onChange={this._setTextValue('comment')}
                                />
                            }
                        </div>}
                    </ModalDialog>
                    : null}
            </div>
        );
    }
}
