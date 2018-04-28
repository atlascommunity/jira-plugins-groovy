//@flow
import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {scriptsReducer} from './rest.reducer';
import {RestRegistryContainer} from './RestRegistryContainer';

import {restService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';
import {ItemActionCreators} from '../common/redux';


const store = createStore(scriptsReducer, {scripts: [], ready: false});

AJS.toInit(() => {
    fixStyle();

    Promise
        .all([restService.getAllScripts(), watcherService.getAllWatches('LISTENER')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <RestRegistryContainer/>
        </Provider>,
        element
    );
});
