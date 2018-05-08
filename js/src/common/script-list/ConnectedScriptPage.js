//@flow
import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import {ScriptPage} from './ScriptPage';


export const ConnectedScriptPage = connect(
    memoizeOne(
        ({items, watches, isReady}: *): * => {
            return {
                items, watches, isReady
            };
        }
    )
)(ScriptPage);
