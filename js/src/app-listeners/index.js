import React from 'react';
import ReactDOM from 'react-dom';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import '../flex.less';
import {ListenerRegistryContainer} from './ListenerRegistryContainer';


AJS.toInit(() => {
    ReactDOM.render(
        <ListenerRegistryContainer/>,
        document.getElementById('react-content')
    );
});
