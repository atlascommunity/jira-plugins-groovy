import ReactDOM from 'react-dom';
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {CustomFieldFormContainer} from './CustomFieldFormContainer';

import '../flex.less';


AJS.toInit(() => {
    const params = new URLSearchParams(window.location.search);

    ReactDOM.render(
        <CustomFieldFormContainer id={parseInt(params.get('fieldConfigId'), 10)}/>,
        document.getElementById('react-content')
    );
});
