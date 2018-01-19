import ReactDOM from 'react-dom';
import React from 'react';

import '../flex.less';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptConsole} from './ScriptConsole';

import {TitleMessages} from '../i18n/common.i18n';


AJS.toInit(() => {
    ReactDOM.render(
        <div>
            <header className="aui-page-header">
                <div className="aui-page-header-inner">
                    <div className="aui-page-header-main">
                        <h2>{TitleMessages.console}</h2>
                    </div>
                </div>
            </header>
            <ScriptConsole/>
        </div>,
        document.getElementById('react-content')
    );
});
