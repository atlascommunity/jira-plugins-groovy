//@flow
import React from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import {RestScript} from './RestScript';
import {RestForm} from './RestForm';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RestMessages} from '../i18n/rest.i18n';

import {withRoot, NotFoundPage, ConnectedScriptPage, ItemViewPage} from '../common/script-list';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {watcherService, restService} from '../service/services';
import {Loader} from '../common/ak/Loader';


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
                {/* $FlowFixMe */}
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
