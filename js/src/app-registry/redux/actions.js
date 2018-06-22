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

export const loadState = (
    directories: KeyedEntities<RegistryDirectoryType>,
    scripts: KeyedEntities<RegistryScriptType>,
    scriptWatches: $ReadOnlyArray<number>,
    directoryWatches: $ReadOnlyArray<number>
): * => ({
    type: LOAD_STATE,
    directories, scripts,
    scriptWatches, directoryWatches
});
export const loadUsage = (items: ScriptUsageItems): * => ({
    type: LOAD_USAGE,
    items
});

export const addScript = (script: RegistryScriptType): * => ({
    type: ADD_SCRIPT,
    script: script
});
export const updateScript = (script: RegistryScriptType): * => ({
    type: UPDATE_SCRIPT,
    script: script
});
export const deleteScript = (id: number): * => ({
    type: DELETE_SCRIPT,
    id: id
});
export const moveScript = (src: number, dst: number, scriptId: number): * => ({
    type: MOVE_SCRIPT,
    src, dst, scriptId
});

export const addDirectory =  (directory: RegistryDirectoryType): * => ({
    type: ADD_DIRECTORY,
    directory: directory
});
export const updateDirectory = (directory: RegistryDirectoryType): * => ({
    type: UPDATE_DIRECTORY,
    directory: directory
});
export const deleteDirectory = (id: number): * => ({
        type: DELETE_DIRECTORY,
        id: id
    });

export const addWatch = (kind: 'script' | 'directory', id: number): * => ({
    type: ADD_WATCH,
    kind, id
});
export const removeWatch =  (kind: 'script' | 'directory', id: number): * => ({
    type: REMOVE_WATCH,
    kind, id
});

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
