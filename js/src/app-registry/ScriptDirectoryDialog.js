import React from 'react';

import {connect} from 'react-redux';

import Dialog from 'aui-react/lib/AUIDialog';
import Button from 'aui-react/lib/AUIButton';

import {RegistryActionCreators} from './registry.reducer';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';


@connect(null, RegistryActionCreators, null, {withRef: true})
export class ScriptDirectoryDialog extends React.Component {
    state = {
        active: false,
        name: '',
        parentId: null,
        id: null
    };

    activateCreate = (parentId) => {
        this.setState({
            active: true,
            name: '',
            parentId: parentId,
            id: null
        });
    };

    activateEdit = (id) => {
        registryService
            .getDirectory(id)
            .then(data => this.setState({
                active: true,
                id: id,
                parentId: null,
                name: data.name
            }));
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
                .then(result => {
                    this.props.updateDirectory(result);
                    this.setState({active: false});
                });
        } else {
            registryService
                .createDirectory(data)
                .then(result => {
                    this.props.addDirectory(result);
                    this.setState({active: false});
                });
        }
    };

    _close = () => this.setState({active: false});

    _setName = (event) => this.setState({ name: event.target.value });

    render() {
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
                            </div>
                        </form>
                    </Dialog>
                    : null}
            </div>
        );
    }
}
