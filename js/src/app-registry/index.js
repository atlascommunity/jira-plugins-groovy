import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptRegistry} from './ScriptRegistry';
import {RegistryActionCreators, registryReducer} from './registry.reducer';

import {registryService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';


const store = createStore(registryReducer, {directories: []});

AJS.toInit(() => {
    fixStyle();

    registryService
        .getAllDirectories()
        .then(directories => store.dispatch(RegistryActionCreators.loadState(directories)));

    ReactDOM.render(
        <Provider store={store}>
            <ScriptRegistry/>
        </Provider>,
        document.getElementById('react-content')
    );
});
