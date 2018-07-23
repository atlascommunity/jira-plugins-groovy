//@flow
import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import {ScriptPage} from './ScriptPage';

import {orderedItemsSelector} from '../redux/selectors';
import {updateFilter} from '../redux';


const itemsSelector = orderedItemsSelector();

export const ConnectedScriptPage = connect(
    memoizeOne(
        ({watches, isReady, ...state}: *): * => {
            return {
                items: itemsSelector(state),
                filter: state.filter,
                watches, isReady
            };
        }
    ),
    { updateFilter }
)(ScriptPage);
