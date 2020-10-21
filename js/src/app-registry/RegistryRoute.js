//@flow
import React from 'react';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import {createStore, applyMiddleware} from 'redux';
//$FlowFixMe
import thunk from 'redux-thunk';

import {ScriptRegistry} from './ScriptRegistry';
import {ScriptForm} from './ScriptForm';
import {ViewScript} from './ViewScript';
import {reducer} from './redux';

import {NotFoundPage} from '../common/script-list/NotFoundPage';


export class RegistryRoute extends React.PureComponent<{}> {
    store = createStore(
        reducer,
        applyMiddleware(thunk)
    );

    render() {
        return (
            <Provider store={this.store}>
                    <Switch>
                        <Route path="/registry" exact={true}>
                            {() => <ScriptRegistry/>}
                        </Route>
                        <Route path="/registry/script/create/" exact={true}>
                            {() =>
                                <ScriptForm isNew={true} id={null} directoryId={-1}/>
                            }
                        </Route>
                        <Route path="/registry/script/create/:directoryId" exact={true}>
                            {({match}) =>
                                <ScriptForm isNew={true} id={null} directoryId={parseInt(match.params.directoryId, 10)}/>
                            }
                        </Route>
                        <Route path="/registry/script/edit/:id">
                            {({match}) =>
                                <ScriptForm isNew={false} id={parseInt(match.params.id, 10)} directoryId={null}/>
                            }
                        </Route>
                        <Route path="/registry/script/view/:id">
                            {({match}) =>
                                <ViewScript id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
            </Provider>
        );
    }
}
