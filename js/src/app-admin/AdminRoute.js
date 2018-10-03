//@flow
import React from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Route, Switch} from 'react-router-dom';

import Button from '@atlaskit/button';

import {AdminScript} from './AdminScript';
import {AdminForm} from './AdminForm';
import {ViewAdminScript} from './ViewAdminScript';

import {RegistryMessages} from '../i18n/registry.i18n';
import {CommonMessages, PageTitleMessages} from '../i18n/common.i18n';
import {Loader} from '../common/ak/Loader';
import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer, filterReducer} from '../common/redux';
import {adminScriptService, watcherService} from '../service';
import {ConnectedScriptPage, NotFoundPage, focusOnRender} from '../common/script-list';
import {RouterLink} from '../common/ak/RouterLink';


const AdminScriptComponent = focusOnRender(AdminScript);

export class AdminRoute extends React.PureComponent<{}> {
    store = createStore(
        combineReducers({
            items: itemsReducer,
            watches: watchesReducer,
            isReady: readinessReducer,
            filter: filterReducer
        })
    );

    componentDidMount() {
        Promise
            .all([adminScriptService.getAllScripts(), watcherService.getAllWatches('ADMIN_SCRIPT')])
            .then(([scripts, watches]) => this.store.dispatch(ItemActionCreators.loadItems(scripts, watches)));
    }

    render() {
        return (
            <Provider store={this.store}>
                <Loader>
                    <Switch>
                        <Route path="/admin-scripts/" exact={true}>
                            {() =>
                                <ConnectedScriptPage
                                    ScriptComponent={AdminScriptComponent}
                                    i18n={{
                                        title: PageTitleMessages.adminScripts,
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

                                            component={RouterLink}
                                            href="/admin-scripts/create"
                                        >
                                            {RegistryMessages.addScript}
                                        </Button>
                                    }
                                />
                            }
                        </Route>
                        <Route path="/admin-scripts/:id/edit" exact={true}>
                            {({match}) =>
                                <AdminForm id={parseInt(match.params.id, 10)} isNew={false}/>
                            }
                        </Route>
                        <Route path="/admin-scripts/:id/view" exact={true}>
                            {({match}) =>
                                <ViewAdminScript id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route path="/admin-scripts/create" exact={true}>
                            {() =>
                                <AdminForm isNew={true} id={null}/>
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
                </Loader>
            </Provider>
        );
    }
}
