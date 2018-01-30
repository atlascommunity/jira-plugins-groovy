import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ExtrasPage} from './ExtrasPage';

import {fixStyle} from '../common/fixStyle';

import '../flex.less';


AJS.toInit(() => {
    fixStyle();

    ReactDOM.render(
        <ExtrasPage/>,
        document.getElementById('react-content')
    );
});
