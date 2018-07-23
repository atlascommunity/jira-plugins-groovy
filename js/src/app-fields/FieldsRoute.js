//@flow
import React from 'react';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import {combineReducers, createStore} from 'redux';

import {FieldScript} from './FieldScript';
import {CustomFieldForm} from './CustomFieldForm';
import {ViewFieldScript} from './ViewFieldScript';

import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';
import {NotFoundPage} from '../common/script-list/NotFoundPage';

import {fieldConfigService, watcherService} from '../service/services';

import {filterReducer, ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {TitleMessages} from '../i18n/common.i18n';
import {ScriptFieldMessages} from '../i18n/cf.i18n';

import {Loader} from '../common/ak/Loader';


export class FieldsRoute extends React.PureComponent<{}> {
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
            .all([fieldConfigService.getAllConfigs(), watcherService.getAllWatches('CUSTOM_FIELD')])
            .then(([scripts, watches]) => this.store.dispatch(ItemActionCreators.loadItems(scripts, watches)));
    }

    render() {
        return (
            <Provider store={this.store}>
                <Loader>
                    <Switch>
                        <Route path="/fields/" exact={true}>
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
                                />
                            }
                        </Route>
                        <Route path="/fields/:id/edit" exact={true}>
                            {({match}) =>
                                <CustomFieldForm id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route path="/fields/:id/view" exact={true}>
                            {({match}) =>
                                <ViewFieldScript id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
                </Loader>
            </Provider>
        );
    }
}
