import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {AuditLogContainer} from './AuditLogContainer';

import '../flex.less';


AJS.toInit(() => {
    ReactDOM.render(
        <AuditLogContainer/>,
        document.getElementById('react-content')
    );
});
