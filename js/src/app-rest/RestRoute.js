//@flow
import React, {type ComponentType} from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import {RestScript} from './RestScript';
import {RestScriptDialog} from './RestScriptDialog';
import {ViewRestScript} from './ViewRestScript';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RestMessages} from '../i18n/rest.i18n';

import type {FullDialogComponentProps} from '../common/script-list/types';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {watcherService, restService} from '../service/services';
import {withRoot} from '../common/script-list/breadcrumbs';
import {Loader} from '../common/ak/Loader';
import {NotFoundPage} from '../common/script-list/NotFoundPage';


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
                                    DialogComponent={(RestScriptDialog: ComponentType<FullDialogComponentProps>)}
                                    ScriptComponent={RestScript}
                                    breadcrumbs={withRoot([])}
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
                        <Route path="/rest/:id/view" exact={true}>
                            {({match}) =>
                                <ViewRestScript id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
                </Loader>
            </Provider>
        );
    }
}
