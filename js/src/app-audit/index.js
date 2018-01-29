import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {AuditLogContainer} from './AuditLogContainer';

import {fixStyle} from '../common/fixStyle';

import '../flex.less';


AJS.toInit(() => {
    fixStyle();

    ReactDOM.render(
        <AuditLogContainer/>,
        document.getElementById('react-content')
    );
});
