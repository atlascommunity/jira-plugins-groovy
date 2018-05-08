//@flow
import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import {ScriptPage} from '../common/script-list/ScriptPage';


export const FieldRegistry = connect(
    memoizeOne(
        ({items, isReady}: *): * => {
            return { items, isReady };
        }
    )
)(ScriptPage);
