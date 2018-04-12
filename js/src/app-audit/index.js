import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {AuditLog} from './AuditLog';

import {fixStyle} from '../common/fixStyle';

import '../flex.less';


AJS.toInit(() => {
    fixStyle();

    ReactDOM.render(
        <AuditLog/>,
        document.getElementById('react-content')
    );
});
