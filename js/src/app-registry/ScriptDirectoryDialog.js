//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';

import {RegistryActionCreators} from './registry.reducer';
import type {BasicRegistryDirectoryType} from './types';

import {ErrorMessage} from '../common/ak/messages';

import {registryService} from '../service/services';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';


type Props = {
    addDirectory: typeof RegistryActionCreators.addDirectory,
    updateDirectory: typeof RegistryActionCreators.updateDirectory
};

type State = {
    active: boolean,
    name: string,
    parentId: ?number,
    id: ?number,
    directory: ?BasicRegistryDirectoryType,
    error: *
};

//todo: declarative activation
export class ScriptDirectoryDialogInternal extends React.PureComponent<Props, State> {
    state = {
        active: false,
        name: '',
        parentId: null,
        id: null,
        directory: null,
        error: null
    };

    activateCreate = (parentId: number) => {
        this.setState({
            active: true,
            name: '',
            parentId: parentId,
            id: null,
            error: null
        });
    };

    activateEdit = (id: number) => {
        registryService
            .getDirectory(id)
            .then(data => this.setState({
                active: true,
                id: id,
                parentId: null,
                name: data.name,
                error: null,
                directory: data
            }));
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
        const {id, name, parentId} = this.state;

        const data = {
            name: name,
            parentId: parentId || undefined
        };

        if (id) {
            registryService
                .updateDirectory(id, data)
                .then(
                    (result: BasicRegistryDirectoryType) => {
                        this.props.updateDirectory(result);
                        this.setState({active: false});
                    },
                    this._handleError);
        } else {
            registryService
                .createDirectory(data)
                .then(
                    (result: BasicRegistryDirectoryType) => {
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

    _setName = (event: SyntheticEvent<HTMLInputElement>) => this.setState({ name: event.currentTarget.value });

    render(): Node {
        const {error, directory} = this.state;

        let errorMessage: * = null;
        let errorField: ?string = null;

        if (error) {
            ({field: errorField, message: errorMessage} = error);
        }

        return (
            <div>
                {this.state.active ?
                    <ModalDialog
                        width="medium"

                        isHeadingMultiline={false}
                        heading={this.state.id ? `${RegistryMessages.editDirectory}: ${directory ? directory.name : ''}` : RegistryMessages.addDirectory}

                        onClose={this._close}
                        actions={[
                            {
                                text: this.state.id ? CommonMessages.update : CommonMessages.create,
                                onClick: this._onSubmit
                            },
                            {
                                text: CommonMessages.cancel,
                                onClick: this._close
                            }
                        ]}
                    >
                        {error && !errorField && <ErrorMessage title={errorMessage}/>}
                        <div className="flex-column">
                            <FieldTextStateless
                                shouldFitContainer={true}
                                required={true}
                                maxLength={32}

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

export const ScriptDirectoryDialog = connect(null, RegistryActionCreators, null, {withRef: true})(ScriptDirectoryDialogInternal);
