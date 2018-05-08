//@flow
import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {AuditLog} from './AuditLog';

import {fixStyle} from '../common/fixStyle';

import '../flex.less';


AJS.toInit(() => {
    fixStyle();

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <AuditLog/>,
        element
    );
});
