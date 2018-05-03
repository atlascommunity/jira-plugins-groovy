//@flow
import {combineReducers} from 'redux';

import {itemsReducer, readinessReducer, watchesReducer} from '../common/redux';


export const tasksReducer = combineReducers({
    items: itemsReducer,
    ready: readinessReducer,
    watches: watchesReducer
});
