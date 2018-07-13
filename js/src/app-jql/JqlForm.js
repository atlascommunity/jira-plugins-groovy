//@flow
import React from 'react';

import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import type {JqlScriptType} from './types';

import {addItem, updateItem} from '../common/redux';
import {jqlScriptService} from '../service/services';

import {RegistryMessages} from '../i18n/registry.i18n';
import {ReturnTypes} from '../common/bindings';
import {ScriptForm, makeScriptForm, type SubmitResult, type ProvidedState} from '../common/script-list/ScriptForm';
import type {DialogComponentProps} from '../common/script-list/types';


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

const editLoader = (id: number) => jqlScriptService
    .getScript(id)
    .then(({name, description, scriptBody}: JqlScriptType): ProvidedState => {
        return {
            values: makeScriptForm({
                description: description || '',
                name, scriptBody
            }),
            name
        };
    });

const onSubmit = (id: ?number, data: {[string]: any}): Promise<SubmitResult> => {
    const promise = id ? jqlScriptService.updateScript(id, data) : jqlScriptService.createScript(data);

    return promise
        .then(
            (item: JqlScriptType): SubmitResult => {
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

class JqlFormInternal extends React.PureComponent<Props> {
    render() {
        return (
            <ScriptForm
                defaultLoader={defaultLoader}
                editLoader={editLoader}
                onSubmit={onSubmit}
                i18n={{
                    editTitle: RegistryMessages.editScript,
                    createTitle: RegistryMessages.addScript,
                    parentName: 'JQL scripts'
                }}
                returnTypes={returnTypes}
                returnTo="/jql/"

                {...this.props}
            />
        );
    }
}

export const JqlForm = withRouter(
    connect(
        null,
        { addItem, updateItem }
    )(JqlFormInternal)
);
