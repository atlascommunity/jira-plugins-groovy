import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ExtrasPage} from './ExtrasPage';

import '../flex.less';


AJS.toInit(() => {
    ReactDOM.render(
        <div>
            <ExtrasPage/>
        </div>,
        document.getElementById('react-content')
    );
});
