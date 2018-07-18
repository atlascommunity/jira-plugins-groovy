//@flow
import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import {ScriptPage} from './ScriptPage';

import {orderedItemsSelector} from '../redux/selectors';


const itemsSelector = orderedItemsSelector();

export const ConnectedScriptPage = connect(
    memoizeOne(
        ({watches, isReady, ...rest}: *): * => {
            return {
                items: itemsSelector(rest),
                watches, isReady
            };
        }
    )
)(ScriptPage);
