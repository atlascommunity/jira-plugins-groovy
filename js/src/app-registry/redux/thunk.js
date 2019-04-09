//@flow
import {type Dispatch} from 'redux';

import {ADD_SCRIPT, UPDATE_SCRIPT} from './actions';

import type {KeyedEntities, RegistryDirectoryType, RegistryScriptType} from '../types';


function makeScriptActionThunk(type: typeof UPDATE_SCRIPT | typeof ADD_SCRIPT): * {
    return (script: RegistryScriptType): * => {
        return (dispatch: Dispatch<any>, getState: *) => {
            const parents = [];

            const directories: KeyedEntities<RegistryDirectoryType> = getState().directories;

            let parent: ?RegistryDirectoryType = directories[script.directoryId];
            while (parent) {
                parents.push(parent.id);
                if (parent.parentId) {
                    parent = directories[parent.parentId];
                } else {
                    parent = null;
                }
            }

            dispatch({
                type, script, parents
            });
        };
    };
}

export const addScript = (makeScriptActionThunk(ADD_SCRIPT));
export const updateScript = makeScriptActionThunk(UPDATE_SCRIPT);
