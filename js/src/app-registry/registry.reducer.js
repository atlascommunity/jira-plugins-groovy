import {combineReducers} from 'redux';


export const registryReducer = combineReducers({
    directories: directoriesReducer
});

const LOAD_STATE = 'LOAD_STATE';

const ADD_DIRECTORY = 'ADD_DIRECTORY';
const UPDATE_DIRECTORY = 'UPDATE_DIRECTORY';
const DELETE_DIRECTORY = 'DELETE_DIRECTORY';

const ADD_SCRIPT = 'ADD_SCRIPT';
const UPDATE_SCRIPT = 'UPDATE_SCRIPT';
const DELETE_SCRIPT = 'DELETE_SCRIPT';


export const RegistryActionCreators = {
    loadState: state => {
        return {
            type: LOAD_STATE,
            state: state
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
    }
};


function directoriesReducer(state, action) {
    console.log(action, state);

    if (state === undefined) {
        return [];
    }

    if (action.type === LOAD_STATE) {
        return action.state;
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
                return action.directory;
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
                    scripts: [...state.scripts, action.script]
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
