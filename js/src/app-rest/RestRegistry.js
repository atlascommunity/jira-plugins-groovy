//@flow
import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import {ScriptPage} from '../common/script-list/ScriptPage';


export const RestRegistry = connect(
    memoizeOne(
        (state: * ): * => {
            return {
                items: state.scripts,
                isReady: state.ready
            };
        }
    )
)(ScriptPage);
