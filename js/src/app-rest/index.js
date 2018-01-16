import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptActionCreators, scriptsReducer} from './rest.reducer';
import {RestRegistryContainer} from './RestRegistryContainer';

import '../flex.less';
import {restService} from '../service/services';


const store = createStore(scriptsReducer, {scripts: [], ready: false});

AJS.toInit(() => {
    restService
        .getAllScripts()
        .then(scripts => store.dispatch(ScriptActionCreators.loadScripts(scripts)));

    ReactDOM.render(
        <div>
            <Provider store={store}>
                <RestRegistryContainer/>
            </Provider>
        </div>,
        document.getElementById('react-content')
    );
});
