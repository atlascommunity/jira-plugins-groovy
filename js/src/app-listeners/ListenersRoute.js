//@flow
import React from 'react';

import {createStore} from 'redux';
import {Provider} from 'react-redux';
import {Route, Switch} from 'react-router-dom';

import {Listener} from './Listener';
import {ListenerDialog} from './ListenerDialog';
import {ViewListener} from './ViewListener';

import {listenersReducer} from './listeners.reducer';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ListenerMessages} from '../i18n/listener.i18n';

import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';
import {withRoot} from '../common/script-list/breadcrumbs';
import {ItemActionCreators} from '../common/redux';

import {jiraService, listenerService, watcherService} from '../service/services';
import type {ObjectMap} from '../common/types';
import {Loader} from '../common/ak/Loader';
import {NotFoundPage} from '../common/script-list/NotFoundPage';


function transformEventTypes(eventTypes: *): ObjectMap {
    const object = {};

    for (const type of eventTypes) {
        object[type.id] = type.name;
    }

    return object;
}

function transformProjects(projects: *): ObjectMap {
    const object = {};

    for (const project of projects) {
        object[project.id] = `${project.key} - ${project.name}`;
    }

    return object;
}

export class ListenersRoute extends React.PureComponent<{}> {
    store = createStore(listenersReducer);

    componentDidMount() {
        Promise
            .all([
                listenerService.getAllListeners(), watcherService.getAllWatches('LISTENER'),
                jiraService.getEventTypes(), jiraService.getAllProjects()
            ])
            .then(
                ([listeners, watches, eventTypes, projects]: *) => {
                    this.store.dispatch(ItemActionCreators.loadItems(
                        listeners, watches,
                        {
                            eventTypes: transformEventTypes(eventTypes),
                            projects: transformProjects(projects)
                        }
                    ));
                }
            );
    }

    render() {
        return (
            <Provider store={this.store}>
                <Loader>
                    <Switch>
                        <Route path="/listeners" exact={true}>
                            {() =>
                                <ConnectedScriptPage
                                    //$FlowFixMe
                                    ScriptComponent={Listener}
                                    //$FlowFixMe
                                    DialogComponent={ListenerDialog}
                                    breadcrumbs={withRoot([])}
                                    i18n={{
                                        addItem: ListenerMessages.addListener,
                                        noItems: ListenerMessages.noListeners,
                                        title: TitleMessages.listeners,
                                        delete: {
                                            heading: ListenerMessages.deleteListener,
                                            areYouSure: CommonMessages.confirmDelete
                                        }
                                    }}
                                />
                            }
                        </Route>
                        <Route path="/listeners/:id/view" exact={true}>
                            {({match}) =>
                                <ViewListener id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route component={NotFoundPage}/>
                    </Switch>
                </Loader>
            </Provider>
        );
    }
}
