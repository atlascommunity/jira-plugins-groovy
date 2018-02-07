import React from 'react';

import {connect} from 'react-redux';

import Message from 'aui-react/lib/AUIMessage';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';

import {RegistryActionCreators} from './registry.reducer';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';


@connect(null, RegistryActionCreators, null, {withRef: true})
export class ScriptDirectoryDialog extends React.Component {
    state = {
        active: false,
        name: '',
        parentId: null,
        id: null,
        error: null
    };

    activateCreate = (parentId) => {
        this.setState({
            active: true,
            name: '',
            parentId: parentId,
            id: null,
            error: null
        });
    };

    activateEdit = (id) => {
        registryService
            .getDirectory(id)
            .then(data => this.setState({
                active: true,
                id: id,
                parentId: null,
                name: data.name,
                error: null
            }));
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

        const id = this.state.id;

        const data = {
            name: this.state.name,
            parentId: this.state.parentId || undefined
        };

        if (id) {
            registryService
                .updateDirectory(id, data)
                .then(
                    result => {
                        this.props.updateDirectory(result);
                        this.setState({active: false});
                    },
                    this._handleError);
        } else {
            registryService
                .createDirectory(data)
                .then(
                    result => {
                        this.props.addDirectory({
                            ...result,
                            children: [],
                            scripts: []
                        });
                        this.setState({active: false});
                    },
                    this._handleError
                );
        }
    };

    _close = () => this.setState({active: false});

    _setName = (event) => this.setState({ name: event.target.value });

    render() {
        const {error} = this.state;

        let errorMessage = null;
        let errorField = null;

        if (error) {
            ({field: errorField, message: errorMessage} = error);
        }

        return (
            <div>
                {this.state.active ?
                    <ModalDialog
                        width="medium"
                        heading="Create directory"
                        onClose={this._close}
                        actions={[
                            {
                                text: CommonMessages.create,
                                onClick: this._onSubmit
                            },
                            {
                                text: CommonMessages.cancel,
                                onClick: this._close
                            }
                        ]}
                    >
                        {error && !errorField ?
                            <Message type="error">
                                {errorMessage}
                            </Message>
                        : null}
                        <div className="flex-column">
                            <FieldTextStateless
                                shouldFitContainer={true}
                                required={true}

                                isInvalid={errorField === 'name'}
                                invalidMessage={errorMessage}

                                label={FieldMessages.name}
                                value={this.state.name}
                                onChange={this._setName}
                            />
                        </div>
                    </ModalDialog>
                    : null}
            </div>
        );
    }
}
