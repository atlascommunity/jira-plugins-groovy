//@flow
import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';
import {BrowserRouter, Switch, Route} from 'react-router-dom';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import EmptyState from '@atlaskit/empty-state';

import {ScriptRegistry} from './ScriptRegistry';
import {ScriptForm} from './ScriptForm';
import {Loader} from './Loader';
import {RegistryActionCreators, registryReducer} from './registry.reducer';

import {registryService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';
import {getBaseUrl} from '../service/ajaxHelper';


const store = createStore(registryReducer, {directories: []});

AJS.toInit(() => {
    fixStyle();

    Promise
        .all([
            registryService.getAllDirectories(),
            watcherService.getAllWatches('REGISTRY_SCRIPT'),
            watcherService.getAllWatches('REGISTRY_DIRECTORY')
        ])
        .then(
            ([tree, scriptWatches, directoryWatches]: *) => {
                store.dispatch(RegistryActionCreators.loadState(tree, scriptWatches, directoryWatches));
            }
        );

    registryService
        .getAllScriptUsage()
        .then(usage => store.dispatch(RegistryActionCreators.loadUsage(usage)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <BrowserRouter basename={`${getBaseUrl()}/plugins/servlet/my-groovy/registry`}>
                <Loader>
                    <Switch>
                        <Route path="/" exact={true} component={ScriptRegistry}/>
                        <Route path="/script/create/" exact={true}>
                            {() =>
                                <ScriptForm isNew={true} id={null} directoryId={-1}/>
                            }
                        </Route>
                        <Route path="/script/create/:directoryId" exact={true}>
                            {({match}) =>
                                <ScriptForm isNew={true} id={null} directoryId={parseInt(match.params.directoryId, 10)}/>
                            }
                        </Route>
                        <Route path="/script/edit/:id">
                            {({match}) =>
                                <ScriptForm isNew={false} id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route>
                            {() => <EmptyState header="Not found" description="¯\_(ツ)_/¯"/>}
                        </Route>
                    </Switch>
                </Loader>
            </BrowserRouter>
        </Provider>,
        element
    );
});
