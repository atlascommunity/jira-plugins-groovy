//@flow
import React from 'react';

import {connect} from 'react-redux';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';

import {addDirectory, updateDirectory} from './redux';
import type {RegistryDirectoryType} from './types';

import {ErrorMessage} from '../common/ak/messages';

import {registryService} from '../service/services';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';
import type {VoidCallback} from '../common/types';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {AsyncPicker, FormField} from '../common/ak';
import type {SingleValueType} from '../common/ak/types';


export type DialogParams = { isNew: boolean, id?: number, parentId?: ?number };

type Props = DialogParams & {
    addDirectory: typeof addDirectory,
    updateDirectory: typeof updateDirectory,
    onClose: VoidCallback
};

type State = {
    name: string,
    parent: ?SingleValueType,
    directory: ?RegistryDirectoryType,
    error: *
};

export class ScriptDirectoryDialogInternal extends React.PureComponent<Props, State> {
    state = {
        name: '',
        parent: null,
        directory: null,
        error: null
    };

    componentDidMount() {
        const {isNew, id, parentId} = this.props;

        if (!isNew) {
            registryService
                //$FlowFixMe id will be non-null here
                .getDirectory(id)
                .then(data => this.setState({
                    name: data.name,
                    parent: data.parentId? {
                        value: data.parentId,
                        label: data.parentName ? data.parentName : data.parentId.toString()
                    } : null,
                    error: null,
                    directory: data
                }));
        } else {
            if (parentId) {
                registryService
                    .getDirectory(parentId)
                    .then(parent => this.setState({
                        name: '',
                        parent: {
                            value: parent.id,
                            label: parent.fullName ? parent.fullName : parent.id.toString()
                        },
                        error: null
                    }));
            } else {
                this.setState({
                    name: '',
                    error: null
                });
            }
        }
    }

    _handleError = (error: *) => {
        const {response} = error;

        if (response.status === 400) {
            this.setState({error: response.data});
        } else {
            throw error;
        }
    };

    _onSubmit = () => {
        const {id, isNew, updateDirectory, addDirectory, onClose} = this.props;
        const {name, parent} = this.state;

        const data = {
            name: name,
            parentId: (parent && parent.value) || undefined
        };

        if (!isNew && id) {
            registryService
                .updateDirectory(id, data)
                .then(
                    (result: RegistryDirectoryType) => {
                        updateDirectory(result);
                        onClose();
                    },
                    this._handleError);
        } else {
            registryService
                .createDirectory(data)
                .then(
                    (result: RegistryDirectoryType) => {
                        addDirectory({
                            ...result,
                            children: [],
                            scripts: []
                        });
                        onClose();
                    },
                    this._handleError
                );
        }
    };

    _setName = (event: SyntheticEvent<HTMLInputElement>) => this.setState({ name: event.currentTarget.value });

    _setParent = (value: ?SingleValueType) => this.setState({ parent: value });

    render() {
        const {onClose, isNew} = this.props;
        const {error, directory, parent} = this.state;

        let errorMessage: * = null;
        let errorField: ?string = null;

        if (error) {
            ({field: errorField, message: errorMessage} = error);
        }

        return (
            <ModalDialog
                width="medium"

                scrollBehavior="outside"
                autoFocus={false}

                isHeadingMultiline={false}
                heading={isNew ? RegistryMessages.addDirectory : `${RegistryMessages.editDirectory}: ${directory ? directory.name : ''}`}

                onClose={onClose}
                actions={[
                    {
                        text: isNew ? CommonMessages.create : CommonMessages.update,
                        onClick: this._onSubmit
                    },
                    {
                        text: CommonMessages.cancel,
                        onClick: onClose
                    }
                ]}
            >
                {error && !errorField && <ErrorMessage title={errorMessage || undefined}/>}
                <div className="flex-column">
                    <FormField
                        label={FieldMessages.parentName}

                        isInvalid={errorField === 'parentId'}
                        invalidMessage={errorMessage || ''}
                    >
                        <AsyncPicker
                            src={`${getPluginBaseUrl()}/registry/directory/picker`}
                            isClearable={true}

                            value={parent}
                            onChange={this._setParent}

                            label=""
                        />
                    </FormField>
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
        );
    }
}

export const ScriptDirectoryDialog = connect(
    () => ({}),
    { addDirectory, updateDirectory }
)(ScriptDirectoryDialogInternal);
