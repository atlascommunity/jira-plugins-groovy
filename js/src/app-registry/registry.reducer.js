import {combineReducers} from 'redux';
import sortBy from 'lodash.sortby';


export const registryReducer = combineReducers({
    directories: directoriesReducer,
    ready: readyReducer,
    scriptWatches: watchesReducer('script'),
    directoryWatches: watchesReducer('directory'),
});

const LOAD_STATE = 'LOAD_STATE';

const ADD_WATCH = 'ADD_WATCHER';
const REMOVE_WATCH = 'REMOVE_WATCH';

const ADD_DIRECTORY = 'ADD_DIRECTORY';
const UPDATE_DIRECTORY = 'UPDATE_DIRECTORY';
const DELETE_DIRECTORY = 'DELETE_DIRECTORY';

const ADD_SCRIPT = 'ADD_SCRIPT';
const UPDATE_SCRIPT = 'UPDATE_SCRIPT';
const DELETE_SCRIPT = 'DELETE_SCRIPT';
const MOVE_SCRIPT = 'MOVE_SCRIPT';


export const RegistryActionCreators = {
    loadState: (tree, scriptWatches, directoryWatches) => {
        return {
            type: LOAD_STATE,
            tree, scriptWatches, directoryWatches
        };
    },
    addWatch: (kind, id) => {
        return {
            type: ADD_WATCH,
            kind, id
        };
    },
    removeWatch: (kind, id) => {
        return {
            type: REMOVE_WATCH,
            kind, id
        };
    },
    addDirectory: directory => {
        return {
            type: ADD_DIRECTORY,
            directory: directory
        };
    },
    updateDirectory: directory => {
        return {
            type: UPDATE_DIRECTORY,
            directory: directory
        };
    },
    deleteDirectory: id => {
        return {
            type: DELETE_DIRECTORY,
            id: id
        };
    },

    addScript: script => {
        return {
            type: ADD_SCRIPT,
            script: script
        };
    },
    updateScript: script => {
        return {
            type: UPDATE_SCRIPT,
            script: script
        };
    },
    deleteScript: id => {
        return {
            type: DELETE_SCRIPT,
            id: id
        };
    },
    moveScript: (src, dst, script) => {
        return {
            type: MOVE_SCRIPT,
            src, dst, script
        };
    }
};

function readyReducer(state, action) {
    if (state === undefined) {
        return false;
    }

    if (action.type === LOAD_STATE) {
        return true;
    }

    return state;
}

function directoriesReducer(state, action) {
    if (state === undefined) {
        return [];
    }

    if (action.type === LOAD_STATE) {
        return action.tree;
    }

    if (action.type === ADD_DIRECTORY && !action.directory.parentId) {
        return [...state, action.directory];
    }

    return state.map(directory => directoryReducer(directory, action)).filter(e => e);
}


function directoryReducer(state, action) {
    let result = state;

    switch (action.type) {
        case ADD_DIRECTORY:
            if (state.id === action.directory.parentId) {
                const directory = action.directory;
                return {
                    ...state,
                    children: [...state.children, {
                        id: directory.id,
                        name: directory.name,
                        children: [],
                        scripts: []
                    }]
                };
            }
            break;
        case UPDATE_DIRECTORY:
            if (state.id === action.directory.id) {
                return {
                    ...action.directory,
                    children: state.children,
                    scripts: state.scripts
                };
            }
            break;
        case DELETE_DIRECTORY:
            if (state.id === action.id) {
                return null;
            }
            break;
        case DELETE_SCRIPT:
            result =  {
                ...state,
                scripts: (state.scripts || []).filter(script => script.id !== action.id)
            };
            break;
        case MOVE_SCRIPT:
            if (state.id === action.src) {
                result = {
                    ...state,
                    scripts: (state.scripts || []).filter(script => script.id !== action.script.id)
                };
            }
            if (state.id === action.dst) {
                result = {
                    ...state,
                    scripts: order([...state.scripts, action.script])
                };
            }
            break;
        case UPDATE_SCRIPT:
            result = {
                ...state,
                scripts: (state.scripts || []).map(script => {
                    if (script.id === action.script.id) {
                        return action.script;
                    } else {
                        return script;
                    }
                })
            };
            break;
        case ADD_SCRIPT:
            if (action.script.directoryId === state.id) {
                result = {
                    ...state,
                    scripts: order([...state.scripts, action.script])
                };
            }
            break;
        default:
            console.debug('unsupported action ' + action.type);
    }

    return {
        ...result,
        children: (result.children || []).map(child => directoryReducer(child, action)).filter(e => e)
    };
}

function watchesReducer(kind) {
    return (state, action) => {
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

function order(items) {
    return sortBy(items, 'name');
}
