//@flow
import React from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Route, Switch} from 'react-router-dom';

import Button from '@atlaskit/button';

import {JqlScript} from './JqlScript';
import {JqlForm} from './JqlForm';
import {ViewJqlScript} from './ViewJqlScript';

import {RegistryMessages} from '../i18n/registry.i18n';
import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {Loader} from '../common/ak/Loader';
import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer, filterReducer} from '../common/redux';
import {jqlScriptService, watcherService} from '../service/services';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';
import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {RouterLink} from '../common/ak/RouterLink';


export class JqlRoute extends React.PureComponent<{}> {
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
            .all([jqlScriptService.getAllScripts(), watcherService.getAllWatches('JQL_FUNCTION')])
            .then(([scripts, watches]) => this.store.dispatch(ItemActionCreators.loadItems(scripts, watches)));
    }

    render() {
        return (
            <Provider store={this.store}>
                <Loader>
                    <Switch>
                        <Route path="/jql/" exact={true}>
                            {() =>
                                <ConnectedScriptPage
                                    ScriptComponent={JqlScript}
                                    i18n={{
                                        title: TitleMessages.jql,
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
                                            href="/jql/create"
                                        >
                                            {RegistryMessages.addScript}
                                        </Button>
                                    }
                                />
                            }
                        </Route>
                        <Route path="/jql/:id/edit" exact={true}>
                            {({match}) =>
                                <JqlForm id={parseInt(match.params.id, 10)} isNew={false}/>
                            }
                        </Route>
                        <Route path="/jql/:id/view" exact={true}>
                            {({match}) =>
                                <ViewJqlScript id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route path="/jql/create" exact={true}>
                            {() =>
                                <JqlForm isNew={true} id={null}/>
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
                </Loader>
            </Provider>
        );
    }
}
