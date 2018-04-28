//@flow
import {combineReducers} from 'redux';

import {itemsReducer, readinessReducer, watchesReducer} from '../common/redux';


export const scriptsReducer = combineReducers({
    scripts: itemsReducer,
    watches: watchesReducer,
    ready: readinessReducer
});
