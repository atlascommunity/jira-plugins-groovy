import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

import LayerManager from '@atlaskit/layer-manager';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {FieldRegistry} from './FieldRegistry';
import {scriptsReducer, ScriptActionCreators} from './fields.reducer';

import {fieldConfigService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';


const store = createStore(scriptsReducer, {scripts: []});

AJS.toInit(() => {
    fixStyle();

    fieldConfigService
        .getAllConfigs()
        .then(scripts => store.dispatch(ScriptActionCreators.loadScripts(scripts)));

    ReactDOM.render(
        <LayerManager>
            <Provider store={store}>
                <FieldRegistry/>
            </Provider>
        </LayerManager>,
        document.getElementById('react-content')
    );
});
