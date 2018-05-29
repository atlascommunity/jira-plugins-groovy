//@flow
import React from 'react';

import {connect} from 'react-redux';

import type {AdminScriptType} from './types';

import {ScriptDialog, makeScriptForm, type SubmitResult} from '../common/script-list/ScriptDialog';
import {ItemActionCreators} from '../common/redux';
import {adminScriptService} from '../service/services';

import type {ProvidedState} from '../common/script-list/ScriptDialog';
import type {FullDialogComponentProps} from '../common/script-list/types';
import {RegistryMessages} from '../i18n/registry.i18n';
import {ReturnTypes} from '../common/bindings';


const {addItem, updateItem} = ItemActionCreators;

type Props = FullDialogComponentProps & {
    addItem: typeof addItem,
    updateItem: typeof updateItem
};

const defaultLoader = () => Promise.resolve(
    {
        values: makeScriptForm({
            name: '',
            description: '',
            comment: ''
        }),
        name: null
    }
);

const editLoader = (id: number) => adminScriptService
    .getScript(id)
    .then(({name, description, scriptBody}: AdminScriptType): ProvidedState => {
        return {
            values: makeScriptForm({
                description: description || '',
                name, scriptBody
            }),
            name
        };
    });

const onSubmit = (id: ?number, data: {[string]: any}): Promise<SubmitResult> => {
    const promise = id ? adminScriptService.updateScript(id, data) : adminScriptService.createScript(data);

    return promise
        .then(
            (item: AdminScriptType): SubmitResult => {
                return {
                    success: true,
                    item
                };
            }
        )
        .catch(
            (error: any): SubmitResult => {
                const {response} = error;

                if (response.status === 400) {
                    return {
                        success: false,
                        error: response.data
                    };
                } else {
                    throw error;
                }
            }
        );
};

const returnTypes = [{
    ...ReturnTypes.string,
    optional: true
}];

class AdminDialogInternal extends React.PureComponent<Props> {
    render() {
        return (
            <ScriptDialog
                defaultLoader={defaultLoader}
                editLoader={editLoader}
                onSubmit={onSubmit}
                i18n={{
                    editTitle: RegistryMessages.editScript,
                    createTitle: RegistryMessages.addScript
                }}
                returnTypes={returnTypes}
                {...this.props}
            />
        );
    }
}

export const AdminDialog = connect(
    null,
    { addItem, updateItem }
)(AdminDialogInternal);
