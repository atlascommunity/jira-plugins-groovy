//@flow
import {combineReducers} from 'redux';

import {
    LOAD_STATE, ADD_DIRECTORY, UPDATE_DIRECTORY, DELETE_DIRECTORY, DELETE_SCRIPT, MOVE_SCRIPT, UPDATE_SCRIPT, ADD_SCRIPT,
    OPEN_DIRECTORY, CLOSE_DIRECTORY, ADD_WATCH, REMOVE_WATCH, UPDATE_FILTER, LOAD_USAGE
} from './actions';
import type {ActionType, DirectoryStateAction} from './actions';

import type {
    FilterType,
    KeyedEntities,
    RegistryDirectoryType,
    RegistryScriptType,
    ScriptUsageItems,
    ScriptUsageType
} from '../types';


export const reducer = combineReducers({
    directories: directoriesReducer,
    scripts: scriptsReducer,
    ready: readyReducer,
    scriptWatches: watchesReducer('script'),
    directoryWatches: watchesReducer('directory'),
    scriptUsage: scriptUsageReducer,
    openDirectories: openDirsReducer,
    filter: filterReducer
});


function readyReducer(state: boolean, action: ActionType): boolean {
    if (state === undefined) {
        return false;
    }

    if (action.type === LOAD_STATE) {
        return true;
    }

    return state;
}

function watchesReducer(kind: 'script'|'directory'): * {
    return (state: $ReadOnlyArray<number>, action: ActionType): $ReadOnlyArray<number> => {
        if (state === undefined) {
            return [];
        }

        if (action.type === LOAD_STATE) {
            return action[`${kind}Watches`];
        }

        if (action.type === ADD_WATCH && action.kind === kind) {
            return [...state, action.id];
        }

        if (action.type === REMOVE_WATCH && action.kind === kind) {
            return state.filter(id => id !== action.id);
        }

        if (action.type === ADD_SCRIPT && kind === 'script') {
            return [...state, action.script.id];
        }

        if (action.type === ADD_DIRECTORY && kind === 'directory') {
            return [...state, action.directory.id];
        }

        return state;
    };
}

type LoadUsageAction = {
    type: typeof LOAD_USAGE,
    items: ScriptUsageItems
};

function scriptUsageReducer(state: ScriptUsageType, action: LoadUsageAction): ScriptUsageType {
    if (state === undefined) {
        return {
            ready: false,
            items: {}
        };
    }

    if (action.type === LOAD_USAGE) {
        return {
            ready: true,
            items: action.items
        };
    }

    return state;
}

function openDirsReducer(state: $ReadOnlyArray<number>, action: DirectoryStateAction): $ReadOnlyArray<number> {
    if (state === undefined) {
        return [];
    }

    if (action.type === OPEN_DIRECTORY) {
        return [...state, action.id];
    } else if (action.type === CLOSE_DIRECTORY) {
        return state.filter(id => id !== action.id);
    }

    return state;
}

function directoriesReducer(state: KeyedEntities<RegistryDirectoryType>, action: *): KeyedEntities<RegistryDirectoryType> {
    if (state === undefined) {
        return {};
    }

    switch (action.type) {
        case LOAD_STATE: {
            return action.directories;
        }
        case ADD_DIRECTORY:
        case UPDATE_DIRECTORY: {
            return {
                ...state,
                [action.directory.id]: action.directory
            };
        }
        case DELETE_DIRECTORY: {
            const {[action.id]: deleted, ...newState} = state;
            return newState;
        }
        default:
            return state;
    }
}

function scriptsReducer(state: KeyedEntities<RegistryScriptType>, action: *): KeyedEntities<RegistryScriptType> {
    if (state === undefined) {
        return {};
    }

    switch (action.type) {
        case LOAD_STATE: {
            return action.scripts;
        }
        case MOVE_SCRIPT:
            return {
                ...state,
                [action.scriptId]: {
                    ...state[action.scriptId],
                    directoryId: action.dst
                }
            };
        case UPDATE_SCRIPT:
        case ADD_SCRIPT:
            return {
                ...state,
                [action.script.id]: action.script
            };
        case DELETE_SCRIPT: {
            const {[action.id]: deleted, ...newState} = state;
            return newState;
        }
        default:
            return state;
    }
}

export type FilterAction = {
    type: typeof UPDATE_FILTER,
    filter: $Shape<FilterType>
};

function filterReducer(state: FilterType, action: FilterAction): FilterType {
    if (state === undefined) {
        return {
            name: '',
            onlyUnused: false
        };
    }

    if (action.type === UPDATE_FILTER) {
        return {
            ...state,
            ...action.filter
        };
    }

    return state;
}
