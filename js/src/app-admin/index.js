//@flow
import ReactDOM from 'react-dom';
import React from 'react';
import {BrowserRouter, Switch, Route, Link} from 'react-router-dom';
import {Provider} from 'react-redux';
import {combineReducers, createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import Button from '@atlaskit/button';

import {AdminScript} from './AdminScript';
import {AdminForm} from './AdminForm';

import {adminScriptService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';

import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

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
        .all([adminScriptService.getAllScripts(), watcherService.getAllWatches('ADMIN_SCRIPT')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <BrowserRouter basename={`${getBaseUrl()}/plugins/servlet/my-groovy/admin-scripts`}>
                <Loader>
                    <Switch>
                        <Route path="/" exact={true}>
                            {() =>
                                <ConnectedScriptPage
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
                                    actions={
                                        <Button
                                            appearance="primary"

                                            component={Link}
                                            to="/create"
                                        >
                                            {RegistryMessages.addScript}
                                        </Button>
                                    }
                                />
                            }
                        </Route>
                        <Route path="/:id/edit" exact={true}>
                            {({match}) =>
                                <AdminForm id={parseInt(match.params.id, 10)} isNew={false}/>
                            }
                        </Route>
                        <Route path="/create" exact={true}>
                            {() =>
                                <AdminForm isNew={true}/>
                            }
                        </Route>
                    </Switch>
                </Loader>
            </BrowserRouter>
        </Provider>,
        element
    );
});
