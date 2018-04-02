import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptRegistry} from './ScriptRegistry';
import {RegistryActionCreators, registryReducer} from './registry.reducer';

import {registryService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';


const store = createStore(registryReducer, {directories: []});

AJS.toInit(() => {
    fixStyle();

    Promise
        .all([
            registryService.getAllDirectories(),
            watcherService.getAllWatches('REGISTRY_SCRIPT'),
            watcherService.getAllWatches('REGISTRY_DIRECTORY')
        ])
        .then(
            ([tree, scriptWatches, directoryWatches]) => {
                store.dispatch(RegistryActionCreators.loadState(tree, scriptWatches, directoryWatches));
            }
        );

    ReactDOM.render(
        <Provider store={store}>
            <ScriptRegistry/>
        </Provider>,
        document.getElementById('react-content')
    );
});
