//@flow
import {combineReducers} from 'redux';

import {itemsReducer, readinessReducer, wholeObjectReducerFactory} from '../common/redux';


export const listenersReducer = combineReducers({
    items: itemsReducer,
    projects: wholeObjectReducerFactory('projects', {}),
    eventTypes: wholeObjectReducerFactory('eventTypes', {}),
    ready: readinessReducer
});
