//@flow
import React, {type ComponentType} from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';

import {RestScript} from './RestScript';
import {RestScriptDialog} from './RestScriptDialog';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RestMessages} from '../i18n/rest.i18n';

import type {FullDialogComponentProps} from '../common/script-list/types';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {watcherService, restService} from '../service/services';
import {withRoot} from '../common/script-list/breadcrumbs';


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
            </Provider>
        );
    }
}
