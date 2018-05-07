//@flow
import ReactDOM from 'react-dom';
import React from 'react';

import {Provider, connect} from 'react-redux';
import {combineReducers, createStore} from 'redux';

import memoizeOne from 'memoize-one';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {AdminScript} from './AdminScript';
import {AdminDialog} from './AdminDialog';

import {adminScriptService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';

import {ScriptPage} from '../common/script-list/ScriptPage';

import {TitleMessages} from '../i18n/common.i18n';

import '../flex.less';
import {RegistryMessages} from '../i18n/registry.i18n';


const ConnectedPage = connect(
    memoizeOne(
        ({items, watches, isReady}: *): * => {
            return {
                items, watches, isReady
            };
        }
    )
)(ScriptPage);


const store = createStore(
    combineReducers({
        items: itemsReducer,
        watches: watchesReducer,
        isReady: readinessReducer
    }),
    {items: [], isReady: false}
);

AJS.toInit(() => {
    fixStyle();

    Promise
        //$FlowFixMe
        .all([adminScriptService.getAllScripts(), watcherService.getAllWatches('ADMIN_SCRIPT')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <ConnectedPage
                DialogComponent={AdminDialog}
                ScriptComponent={AdminScript}
                i18n={{
                    title: TitleMessages.adminScripts,
                    addItem: RegistryMessages.addScript,
                    noItems: RegistryMessages.noScripts
                }}
            />
        </Provider>,
        element
    );
});
