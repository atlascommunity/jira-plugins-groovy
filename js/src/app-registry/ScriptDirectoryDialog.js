import React from 'react';

import {connect} from 'react-redux';

import Dialog from 'aui-react/lib/AUIDialog';
import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

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
                        this.props.addDirectory(result);
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
                    <Dialog
                        size="medium"
                        titleContent="Create directory"
                        onClose={this._close}
                        footerActionContent={[
                            <Button key="create" onClick={this._onSubmit}>{CommonMessages.create}</Button>,
                            <Button key="close" type="link" onClick={this._close}>{CommonMessages.cancel}</Button>
                        ]}
                        type="modal"
                        styles={{zIndex: '3000'}}
                    >
                        {error && !errorField ?
                            <Message type="error">
                                {errorMessage}
                            </Message>
                        : null}
                        <form className="aui" onSubmit={this._onSubmit}>
                            <div className="field-group">
                                <label htmlFor="directory-dialog-name">
                                    {FieldMessages.name} <span className="aui-icon icon-required"/>
                                </label>
                                <input
                                    type="text"
                                    className="text long-field"
                                    id="directory-dialog-name"
                                    value={this.state.name}
                                    onChange={this._setName}
                                />
                                {errorField === 'name' && <div className="error">{errorMessage}</div>}
                            </div>
                        </form>
                    </Dialog>
                    : null}
            </div>
        );
    }
}
