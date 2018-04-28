//@flow
import {combineReducers} from 'redux';

import {itemsReducer, readinessReducer, watchesReducer, wholeObjectReducerFactory} from '../common/redux';


export const listenersReducer = combineReducers({
    items: itemsReducer,
    watches: watchesReducer,
    projects: wholeObjectReducerFactory('projects', {}),
    eventTypes: wholeObjectReducerFactory('eventTypes', {}),
    ready: readinessReducer
});
