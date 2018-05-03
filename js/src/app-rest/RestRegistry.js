//@flow
import {connect} from 'react-redux';

import {ScriptPage} from '../common/script-list/ScriptPage';


export const RestRegistry = connect(
    (state: * ): * => {
        return {
            items: state.scripts,
            isReady: state.ready
        };
    }
)(ScriptPage);
