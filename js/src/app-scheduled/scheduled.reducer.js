import {combineReducers} from 'redux';


const LOAD_TASKS = 'LOAD_TASKS';

const ADD_TASK = 'ADD_TASK';
const UPDATE_TASK = 'UPDATE_TASK';
const DELETE_TASK = 'DELETE_TASK';

export const tasksReducer = combineReducers({
    tasks: taskReducer,
    ready: readyReducer
});

export const TaskActionCreators = {
    loadTasks: tasks => {
        return {
            type: LOAD_TASKS,
            value: tasks
        };
    },
    addTask: task => {
        return {
            type: ADD_TASK,
            task: task
        };
    },
    updateTask: task => {
        return {
            type: UPDATE_TASK,
            task: task
        };
    },
    deleteTask: id => {
        return {
            type: DELETE_TASK,
            id: id
        };
    }
};

function taskReducer(state, action) {
    if (state === undefined) {
        return [];
    }

    // eslint-disable-next-line default-case
    switch(action.type) {
        case LOAD_TASKS:
            return action.value;
        case ADD_TASK:
            return [...state, action.task];
        case UPDATE_TASK:
            return state.map(task => {
                if (task.id === action.task.id) {
                    return action.task;
                }
                return task;
            });
        case DELETE_TASK:
            return state.filter(task => task.id !== action.id);
    }

    return state;
}

function readyReducer(state, action) {
    if (state === undefined) {
        return false;
    }

    if (action.type === LOAD_TASKS) {
        return true;
    }

    return state;
}
