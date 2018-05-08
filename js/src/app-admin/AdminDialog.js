//@flow
import * as React from 'react';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import type {AdminScriptType} from './types';

import {ScriptDialog, type SubmitResult} from '../common/script-list/ScriptDialog';
import {ItemActionCreators} from '../common/redux';
import {adminScriptService} from '../service/services';

import type {ProvidedState} from '../common/script-list/ScriptDialog';
import type {FullDialogComponentProps} from '../common/script-list/types';
import {RegistryMessages} from '../i18n/registry.i18n';


type Props = FullDialogComponentProps & {
    addItem: typeof ItemActionCreators.addItem,
    updateItem: typeof ItemActionCreators.updateItem
};

const defaultLoader = () => Promise.resolve(
    {
        values: Map({
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
            values: Map({
                name, description, scriptBody
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

class AdminDialogInternal extends React.PureComponent<Props> {
    render(): React.Node {
        return (
            <ScriptDialog
                defaultLoader={defaultLoader}
                editLoader={editLoader}
                onSubmit={onSubmit}
                i18n={{
                    editTitle: RegistryMessages.editScript,
                    createTitle: RegistryMessages.addScript
                }}
                {...this.props}
            />
        );
    }
}

export const AdminDialog = connect(
    null,
    {
        addItem: ItemActionCreators.addItem,
        updateItem: ItemActionCreators.updateItem
    }
)(AdminDialogInternal);
