import ReactDOM from 'react-dom';
import React from 'react';

import '../flex.less';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptConsole} from './ScriptConsole';


AJS.toInit(() => {
    ReactDOM.render(
        <div className="content-container">
            <div className="content-body">
                <ScriptConsole/>
            </div>
        </div>,
        document.getElementById('react-content')
    );
});
