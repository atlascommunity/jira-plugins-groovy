//@flow
import React from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import Button from '@atlaskit/button';

import {RestScript} from './RestScript';
import {RestForm} from './RestForm';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RestMessages} from '../i18n/rest.i18n';

import {NotFoundPage, ConnectedScriptPage, ItemViewPage} from '../common/script-list';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {watcherService, restService} from '../service/services';
import {Loader} from '../common/ak/Loader';
import {RouterLink} from '../common/ak/RouterLink';
import {RegistryMessages} from '../i18n/registry.i18n';


export class RestRoute extends React.PureComponent<{}> {
    store = createStore(
        combineReducers({
            items: itemsReducer,
            watches: watchesReducer,
            isReady: readinessReducer
        })
    );

    componentDidMount() {
        Promise
            .all([restService.getAllScripts(), watcherService.getAllWatches('LISTENER')])
            .then(([scripts, watches]) => this.store.dispatch(ItemActionCreators.loadItems(scripts, watches)));
    }

    render() {
        return (
            <Provider store={this.store}>
                <Loader>
                    <Switch>
                        <Route path="/rest/" exact={true}>
                            {() =>
                                <ConnectedScriptPage
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
                                    actions={
                                        <Button
                                            appearance="primary"

                                            component={RouterLink}
                                            href="/rest/create"
                                        >
                                            {RegistryMessages.addScript}
                                        </Button>
                                    }
                                />
                            }
                        </Route>
                        <Route path="/rest/:id/edit" exact={true}>
                            {({match}) =>
                                <RestForm isNew={false} id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route path="/rest/:id/view" exact={true}>
                            {({match}) =>
                                <ItemViewPage
                                    id={parseInt(match.params.id, 10)}

                                    //$FlowFixMe
                                    ScriptComponent={RestScript}
                                    deleteCallback={restService.deleteScript}
                                    i18n={{
                                        deleteDialogTitle: RestMessages.deleteScript,
                                        parentName: 'REST scripts'
                                    }}
                                    parentLocation="/rest/"
                                />
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
                </Loader>
            </Provider>
        );
    }
}
