//@flow
import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {scriptsReducer} from './rest.reducer';
import {RestRegistry} from './RestRegistry';
import {RestScript} from './RestScript';
import {RestScriptDialog} from './RestScriptDialog';

import {restService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';
import {ItemActionCreators} from '../common/redux';

import {TitleMessages} from '../i18n/common.i18n';
import {RestMessages} from '../i18n/rest.i18n';

import '../flex.less';


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
            <RestRegistry
                DialogComponent={RestScriptDialog}
                ScriptComponent={RestScript}
                i18n={{
                    title: TitleMessages.rest,
                    addItem: RestMessages.addScript,
                    noItems: RestMessages.noScripts
                }}
            />
        </Provider>,
        element
    );
});
