//@flow
import React from 'react';

import {createStore} from 'redux';
import {Provider} from 'react-redux';
import {Route, Switch} from 'react-router-dom';

import Button from '@atlaskit/button';

import {Listener} from './Listener';
import {ListenerForm} from './ListenerForm';

import {listenersReducer} from './listeners.reducer';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ListenerMessages} from '../i18n/listener.i18n';

import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';
import {withRoot} from '../common/script-list/breadcrumbs';
import {ItemActionCreators} from '../common/redux';

import {NotFoundPage, ItemViewPage} from '../common/script-list';
import {Loader, RouterLink} from '../common/ak';

import {jiraService, listenerService, watcherService} from '../service/services';
import type {ObjectMap} from '../common/types';


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
                                    ScriptComponent={Listener}
                                    i18n={{
                                        addItem: ListenerMessages.addListener,
                                        noItems: ListenerMessages.noListeners,
                                        title: TitleMessages.listeners,
                                        delete: {
                                            heading: ListenerMessages.deleteListener,
                                            areYouSure: CommonMessages.confirmDelete
                                        }
                                    }}
                                    actions={
                                        <Button
                                            appearance="primary"

                                            component={RouterLink}
                                            href="/listeners/create"
                                        >
                                            {ListenerMessages.addListener}
                                        </Button>
                                    }
                                />
                            }
                        </Route>
                        <Route path="/listeners/create">
                            {() =>
                                <ListenerForm isNew={true} id={null}/>
                            }
                        </Route>
                        <Route path="/listeners/:id/edit" exact={true}>
                            {({match}) =>
                                <ListenerForm isNew={false} id={parseInt(match.params.id, 10)}/>
                            }
                        </Route>
                        <Route path="/listeners/:id/view" exact={true}>
                            {({match}) =>
                                <ItemViewPage
                                    id={parseInt(match.params.id, 10)}

                                    ScriptComponent={Listener}
                                    deleteCallback={listenerService.deleteListener}
                                    i18n={{
                                        deleteDialogTitle: ListenerMessages.deleteListener,
                                        parentName: 'Listeners'
                                    }}
                                    parentLocation="/listeners/"
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
