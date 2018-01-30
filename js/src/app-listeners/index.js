import React from 'react';
import ReactDOM from 'react-dom';

import {createStore} from 'redux';
import {Provider} from 'react-redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ListenerActionCreators, listenersReducer} from './listeners.reducer';
import {ListenerRegistryContainer} from './ListenerRegistryContainer';

import {jiraService, listenerService} from '../service/services';

import '../flex.less';
import {fillListenerKeys} from '../model/listener.model';
import {fixStyle} from '../common/fixStyle';


AJS.toInit(() => {
    fixStyle();

    const store = createStore(listenersReducer, {
        ready: {
            listeners: false,
            projects: false,
            events: false
        },
        listeners: []
    });

    jiraService.getEventTypes().then(eventTypes => {
        const object = {};

        for (const type of eventTypes) {
            object[type.id] = type.name;
        }

        store.dispatch(ListenerActionCreators.loadEventTypes(object));
    });

    jiraService.getAllProjects().then(projects => {
        const object = {};

        for (const project of projects) {
            object[project.id] = `${project.key} - ${project.name}`;
        }

        store.dispatch(ListenerActionCreators.loadProjects(object));
    });

    listenerService
        .getAllListeners()
        .then(listeners => store.dispatch(ListenerActionCreators.loadListeners(
            listeners.map(listener => fillListenerKeys(listener))
        )));


    ReactDOM.render(
        <Provider store={store}>
            <ListenerRegistryContainer/>
        </Provider>,
        document.getElementById('react-content')
    );
});
