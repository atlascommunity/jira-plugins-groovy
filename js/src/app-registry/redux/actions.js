//@flow
import type {RegistryScriptType, KeyedEntities, RegistryDirectoryType, ScriptUsageItems, FilterType} from '../types';


export const LOAD_STATE = 'LOAD_STATE';

export const ADD_WATCH = 'ADD_WATCHER';
export const REMOVE_WATCH = 'REMOVE_WATCH';

export const ADD_DIRECTORY = 'ADD_DIRECTORY';
export const UPDATE_DIRECTORY = 'UPDATE_DIRECTORY';
export const DELETE_DIRECTORY = 'DELETE_DIRECTORY';

export const ADD_SCRIPT = 'ADD_SCRIPT';
export const UPDATE_SCRIPT = 'UPDATE_SCRIPT';
export const DELETE_SCRIPT = 'DELETE_SCRIPT';
export const MOVE_SCRIPT = 'MOVE_SCRIPT';

export const LOAD_USAGE = 'LOAD_USAGE';

export const UPDATE_FILTER = 'UPDATE_FILTER';

export const OPEN_DIRECTORY = 'OPEN_DIRECTORY';
export const CLOSE_DIRECTORY = 'CLOSE_DIRECTORY';


export type ActionType = any;

type AddDirectoryAction = {
    type: typeof ADD_DIRECTORY,
    directory: RegistryDirectoryType
};

type UpdateDirectoryAction = {
    type: typeof UPDATE_DIRECTORY,
    directory: RegistryDirectoryType
};

export const directoryRelatedActions = [
    LOAD_STATE, ADD_DIRECTORY, UPDATE_DIRECTORY, DELETE_DIRECTORY, DELETE_SCRIPT, MOVE_SCRIPT, UPDATE_SCRIPT, ADD_SCRIPT
];

export const RegistryActionCreators = {
    loadState: (
        directories: KeyedEntities<RegistryDirectoryType>,
        scripts: KeyedEntities<RegistryScriptType>,
        scriptWatches: $ReadOnlyArray<number>,
        directoryWatches: $ReadOnlyArray<number>
    ): * => {
        return {
            type: LOAD_STATE,
            directories, scripts,
            scriptWatches, directoryWatches
        };
    },
    addWatch: (kind: 'script' | 'directory', id: number): * => {
        return {
            type: ADD_WATCH,
            kind, id
        };
    },
    removeWatch: (kind: 'script' | 'directory', id: number): * => {
        return {
            type: REMOVE_WATCH,
            kind, id
        };
    },
    addDirectory: (directory: RegistryDirectoryType): AddDirectoryAction => {
        return {
            type: ADD_DIRECTORY,
            directory: directory
        };
    },
    updateDirectory: (directory: RegistryDirectoryType): UpdateDirectoryAction => {
        return {
            type: UPDATE_DIRECTORY,
            directory: directory
        };
    },
    deleteDirectory: (id: number): * => {
        return {
            type: DELETE_DIRECTORY,
            id: id
        };
    },

    addScript: (script: RegistryScriptType): * => {
        return {
            type: ADD_SCRIPT,
            script: script
        };
    },
    updateScript: (script: RegistryScriptType): * => {
        return {
            type: UPDATE_SCRIPT,
            script: script
        };
    },
    deleteScript: (id: number): * => {
        return {
            type: DELETE_SCRIPT,
            id: id
        };
    },
    moveScript: (src: number, dst: number, scriptId: number): * => {
        return {
            type: MOVE_SCRIPT,
            src, dst, scriptId
        };
    },
    loadUsage: (items: ScriptUsageItems): * => {
        return {
            type: LOAD_USAGE,
            items
        };
    }
};

export type DirectoryStateAction = {
    type: typeof OPEN_DIRECTORY | typeof CLOSE_DIRECTORY,
    id: number
};

export const DirectoryStateActionCreators = {
    open: (id: number): DirectoryStateAction => {
        return {
            type: OPEN_DIRECTORY,
            id
        };
    },
    close: (id: number): DirectoryStateAction => {
        return {
            type: CLOSE_DIRECTORY,
            id
        };
    }
};

export const UpdateActionCreators = {
    updateFilter: (filter: $Shape<FilterType>) => ({
        type: UPDATE_FILTER, filter
    })
};
