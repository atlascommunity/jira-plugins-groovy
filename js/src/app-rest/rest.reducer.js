import {combineReducers} from 'redux';


const LOAD_SCRIPTS = 'LOAD_SCRIPTS';

const ADD_SCRIPT = 'ADD_SCRIPT';
const UPDATE_SCRIPT = 'UPDATE_SCRIPT';
const DELETE_SCRIPT = 'DELETE_SCRIPT';

export const scriptsReducer = combineReducers({
    scripts: scriptReducer
});

export const ScriptActionCreators = {
    loadScripts: scripts => {
        return {
            type: LOAD_SCRIPTS,
            value: scripts
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

function scriptReducer(state, action) {
    if (state === undefined) {
        return [];
    }

    // eslint-disable-next-line default-case
    switch(action.type) {
        case LOAD_SCRIPTS:
            return action.value;
        case ADD_SCRIPT:
            return [...state, action.script];
        case UPDATE_SCRIPT:
            return state.map(script => {
                if (script.id === action.script.id) {
                    return action.script;
                }
                return script;
            });
        case DELETE_SCRIPT:
            return state.filter(script => script.id !== action.id);
    }

    return state;
}
