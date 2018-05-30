//@flow
import ReactDOM from 'react-dom';
import React, {type ComponentType} from 'react';

import {Provider} from 'react-redux';
import {combineReducers, createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {AdminScript} from './AdminScript';
import {AdminDialog} from './AdminDialog';

import {adminScriptService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';

import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import type {FullDialogComponentProps} from '../common/script-list/types';

import '../flex.less';


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
        .all([adminScriptService.getAllScripts(), watcherService.getAllWatches('ADMIN_SCRIPT')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <ConnectedScriptPage
                DialogComponent={(AdminDialog: ComponentType<FullDialogComponentProps>)}
                ScriptComponent={AdminScript}
                i18n={{
                    title: TitleMessages.adminScripts,
                    addItem: RegistryMessages.addScript,
                    noItems: RegistryMessages.noScripts,
                    delete: {
                        heading: RegistryMessages.deleteScript,
                        areYouSure: CommonMessages.confirmDelete
                    }
                }}
            />
        </Provider>,
        element
    );
});
