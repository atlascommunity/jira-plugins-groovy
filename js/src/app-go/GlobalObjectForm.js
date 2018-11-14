//@flow
import React from 'react';

import {connect} from 'react-redux';
import {withRouter, type RouterHistory} from 'react-router-dom';

import {Record} from 'immutable';

import type {GlobalObjectScriptType} from './types';

import {addItem, updateItem} from '../common/redux';
import {globalObjectService} from '../service';

import {RegistryMessages} from '../i18n/registry.i18n';
import {ScriptForm, type SubmitResult} from '../common/script-list/ScriptForm';

import type {ProvidedState} from '../common/script-list/ScriptForm';
import type {DialogComponentProps, ScriptForm as ScriptFormType} from '../common/script-list/types';


type Props = {|
    ...DialogComponentProps,
    history: RouterHistory,
    addItem: typeof addItem,
    updateItem: typeof updateItem
|};

const recordFactory = Record({
    name: '',
    description: '',
    scriptBody: '',
    comment: ''
});

const defaultLoader = () => Promise.resolve(
    {
        values: recordFactory(),
        name: null
    }
);

const editLoader = (id: number) => globalObjectService
    .getScript(id)
    .then(({name, description, scriptBody}: GlobalObjectScriptType): ProvidedState<ScriptFormType> => {
        return {
            values: recordFactory({
                description: description || '',
                name, scriptBody
            }),
            name
        };
    });

const onSubmit = (id: ?number, data: {[string]: any}): Promise<SubmitResult> => {
    const promise = id ? globalObjectService.updateScript(id, data) : globalObjectService.createScript(data);

    return promise
        .then(
            (item: GlobalObjectScriptType): SubmitResult => {
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

class GlobalObjectFormInternal extends React.PureComponent<Props> {
    render() {
        return (
            <ScriptForm
                defaultLoader={defaultLoader}
                editLoader={editLoader}

                onSubmit={onSubmit}
                i18n={{
                    editTitle: RegistryMessages.editScript,
                    createTitle: RegistryMessages.addScript,
                    parentName: 'Global objects'
                }}
                scriptType="GLOBAL_OBJECT"
                returnTo="/go/"
                bindings={null}

                {...this.props}
            />
        );
    }
}

export const GlobalObjectForm = withRouter(
    connect(
        null,
        { addItem, updateItem }
    )(GlobalObjectFormInternal)
);
