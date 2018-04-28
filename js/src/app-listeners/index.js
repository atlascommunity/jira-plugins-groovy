//@flow
import React from 'react';
import ReactDOM from 'react-dom';

import {createStore} from 'redux';
import {Provider} from 'react-redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {listenersReducer} from './listeners.reducer';
import {ListenerRegistryContainer} from './ListenerRegistryContainer';

import {jiraService, listenerService, watcherService} from '../service/services';

import '../flex.less';
import {fixStyle} from '../common/fixStyle';
import {ItemActionCreators} from '../common/redux';
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

AJS.toInit(() => {
    fixStyle();

    const store = createStore(listenersReducer);

    Promise
        .all([
            listenerService.getAllListeners(), watcherService.getAllWatches('LISTENER'),
            jiraService.getEventTypes(), jiraService.getAllProjects()
        ])
        .then(
            ([listeners, watches, eventTypes, projects]: *) => {
                store.dispatch(ItemActionCreators.loadItems(
                    listeners, watches,
                    {
                        eventTypes: transformEventTypes(eventTypes),
                        projects: transformProjects(projects)
                    }
                ));
            }
        );

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <ListenerRegistryContainer/>
        </Provider>,
        element
    );
});
