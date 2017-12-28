import {combineReducers} from 'redux';


const LOAD_PROJECTS = 'LOAD_PROJECTS';
const LOAD_EVENT_TYPES = 'LOAD_EVENT_TYPES';
const LOAD_LISTENERS = 'LOAD_LISTENERS';

const ADD_LISTENER = 'ADD_LISTENER';
const UPDATE_LISTENER = 'UPDATE_LISTENER';
const DELETE_LISTENER = 'DELETE_LISTENER';

export const listenersReducer = combineReducers({
    listeners: listenerReducer,
    projects: wholeObjectReducer(LOAD_PROJECTS),
    eventTypes: wholeObjectReducer(LOAD_EVENT_TYPES),
    ready: readinessReducer
});

export const ListenerActionCreators = {
    loadProjects: projects => {
        return {
            type: LOAD_PROJECTS,
            value: projects
        };
    },
    loadEventTypes: eventTypes => {
        return {
            type: LOAD_EVENT_TYPES,
            value: eventTypes
        };
    },
    loadListeners: listeners => {
        return {
            type: LOAD_LISTENERS,
            value: listeners
        };
    },
    addListener: listener => {
        return {
            type: ADD_LISTENER,
            listener: listener
        };
    },
    updateListener: listener => {
        return {
            type: UPDATE_LISTENER,
            listener: listener
        };
    },
    deleteListener: id => {
        return {
            type: DELETE_LISTENER,
            id: id
        };
    }
};

function readinessReducer(state, action) {
    if (state === undefined) {
        return {
            listeners: false,
            projects: false,
            events: false
        };
    }

    // eslint-disable-next-line default-case
    switch(action.type) {
        case LOAD_LISTENERS:
            return {
                ...state,
                listeners: true
            };
        case LOAD_EVENT_TYPES:
            return {
                ...state,
                events: true
            };
        case LOAD_PROJECTS:
            return {
                ...state,
                projects: true
            };
    }

    return state;
}

function listenerReducer(state, action) {
    if (state === undefined) {
        return [];
    }

    // eslint-disable-next-line default-case
    switch(action.type) {
        case LOAD_LISTENERS:
            return action.value;
        case ADD_LISTENER:
            return [...state, action.listener];
        case UPDATE_LISTENER:
            return state.map(listener => {
                if (listener.id === action.listener.id) {
                    return action.listener;
                }
                return listener;
            });
        case DELETE_LISTENER:
            return state.filter(listener => listener.id !== action.id);
    }

    return state;
}

function wholeObjectReducer(updateAction) {
    return function(state, action) {
        if (state === undefined) {
            return {};
        }

        if (action.type === updateAction) {
            return action.value;
        }

        return state;
    };
}
