//@flow
import ReactDOM from 'react-dom';
import React, {type ComponentType} from 'react';
import {Provider} from 'react-redux';

import {combineReducers, createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {RestScript} from './RestScript';
import {RestScriptDialog} from './RestScriptDialog';

import {restService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';
import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RestMessages} from '../i18n/rest.i18n';

import '../flex.less';
import type {FullDialogComponentProps} from '../common/script-list/types';


const store = createStore(
    combineReducers({
        items: itemsReducer,
        watches: watchesReducer,
        isReady: readinessReducer
    })
);

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
            <ConnectedScriptPage
                DialogComponent={(RestScriptDialog: ComponentType<FullDialogComponentProps>)}
                ScriptComponent={RestScript}
                i18n={{
                    title: TitleMessages.rest,
                    addItem: RestMessages.addScript,
                    noItems: RestMessages.noScripts,
                    delete: {
                        heading: RestMessages.deleteScript,
                        areYouSure: CommonMessages.confirmDelete
                    }
                }}
            />
        </Provider>,
        element
    );
});
