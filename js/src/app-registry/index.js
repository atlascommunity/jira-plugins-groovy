import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptRegistry} from './ScriptRegistry';
import {RegistryActionCreators, registryReducer} from './registry.reducer';

import {registryService} from '../service/services';

import '../flex.less';


const store = createStore(registryReducer, {directories: []});

AJS.toInit(() => {
    //todo: create promise api for $.ajax
    registryService
        .getAllDirectories()
        .then(directories => store.dispatch(RegistryActionCreators.loadState(directories)));

    ReactDOM.render(
        <div>
            <Provider store={store}>
                <ScriptRegistry/>
            </Provider>
        </div>,
        document.getElementById('react-content')
    );
});
