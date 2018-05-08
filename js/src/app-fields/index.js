//@flow
import ReactDOM from 'react-dom';
import React from 'react';

import {Provider} from 'react-redux';

import {combineReducers, createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {FieldScript} from './FieldScript';
import {FieldRegistry} from './FieldRegistry';

import {fieldConfigService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';
import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {TitleMessages} from '../i18n/common.i18n';
import {ScriptFieldMessages} from '../i18n/cf.i18n';


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
        .all([fieldConfigService.getAllConfigs(), watcherService.getAllWatches('CUSTOM_FIELD')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <FieldRegistry
                ScriptComponent={FieldScript}
                i18n={{
                    title: TitleMessages.fields,
                    addItem: '',
                    noItems: ScriptFieldMessages.noFields
                }}

                isCreateDisabled={true}
            />
        </Provider>,
        element
    );
});
