//@flow
import React from 'react';

import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import type {AdminScriptType} from './types';

import {makeScriptForm, type SubmitResult} from '../common/script-list/ScriptDialog';
import {addItem, updateItem} from '../common/redux';
import {adminScriptService} from '../service/services';

import {RegistryMessages} from '../i18n/registry.i18n';
import {ReturnTypes} from '../common/bindings';
import {ScriptForm} from '../common/script-list/ScriptForm';
import type {ProvidedState} from '../common/script-list/ScriptDialog';
import type {DialogComponentProps} from '../common/script-list/types';
import {withRoot} from '../common/script-list/breadcrumbs';
import {RouterLink} from '../common/ak/RouterLink';


type Props = DialogComponentProps & {
    history: any,
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
            <ScriptForm
                defaultLoader={defaultLoader}
                editLoader={editLoader}
                onSubmit={onSubmit}
                i18n={{
                    editTitle: RegistryMessages.editScript,
                    createTitle: RegistryMessages.addScript,
                    parentName: 'Admin scripts'
                }}
                returnTypes={returnTypes}
                returnTo="/admin-scripts/"

                {...this.props}
            />
        );
    }
}

export const AdminForm = withRouter(
    connect(
        null,
        { addItem, updateItem }
    )(AdminDialogInternal)
);
