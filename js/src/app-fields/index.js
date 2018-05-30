//@flow
import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';
import {BrowserRouter, Switch, Route} from 'react-router-dom';

import {combineReducers, createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {FieldScript} from './FieldScript';
import {CustomFieldForm} from './CustomFieldForm';

import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import {fieldConfigService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {TitleMessages} from '../i18n/common.i18n';
import {ScriptFieldMessages} from '../i18n/cf.i18n';

import {getBaseUrl} from '../service/ajaxHelper';
import {Loader} from '../common/ak/Loader';

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
        .all([fieldConfigService.getAllConfigs(), watcherService.getAllWatches('CUSTOM_FIELD')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <BrowserRouter basename={`${getBaseUrl()}/plugins/servlet/my-groovy/fields`}>
                <Loader>
                    <Switch>
                        <Route path="/" exact={true}>
                            {() =>
                                <ConnectedScriptPage
                                    ScriptComponent={FieldScript}
                                    i18n={{
                                        title: TitleMessages.fields,
                                        addItem: '',
                                        noItems: ScriptFieldMessages.noFields,
                                        delete: {
                                            heading: '',
                                            areYouSure: () => ''
                                        }
                                    }}

                                    isCreateDisabled={true}
                                />
                            }
                        </Route>
                        <Route path="/:id/edit" exact={true}>
                            {({match}) =>
                                <CustomFieldForm id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                    </Switch>
                </Loader>
            </BrowserRouter>
        </Provider>,
        element
    );
});
