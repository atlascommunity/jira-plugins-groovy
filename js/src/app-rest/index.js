import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScriptActionCreators, scriptsReducer} from './rest.reducer';
import {RestRegistryContainer} from './RestRegistryContainer';

import {restService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';


const store = createStore(scriptsReducer, {scripts: [], ready: false});

AJS.toInit(() => {
    fixStyle();

    restService
        .getAllScripts()
        .then(scripts => store.dispatch(ScriptActionCreators.loadScripts(scripts)));

    ReactDOM.render(
        <Provider store={store}>
            <RestRegistryContainer/>
        </Provider>,
        document.getElementById('react-content')
    );
});
