import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {FieldRegistry} from './FieldRegistry';
import {scriptsReducer, ScriptActionCreators} from './fields.reducer';

import {fieldConfigService} from '../service/services';

import '../flex.less';


const store = createStore(scriptsReducer, {scripts: []});

AJS.toInit(() => {
    fieldConfigService
        .getAllConfigs()
        .then(scripts => store.dispatch(ScriptActionCreators.loadScripts(scripts)));

    ReactDOM.render(
        <div>
            <Provider store={store}>
                <FieldRegistry/>
            </Provider>
        </div>,
        document.getElementById('react-content')
    );
});
