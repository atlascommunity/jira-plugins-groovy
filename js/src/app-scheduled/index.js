import ReactDOM from 'react-dom';
import React from 'react';
import {Provider} from 'react-redux';

import {createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {TaskActionCreators, tasksReducer} from './scheduled.reducer';
import {ScheduledTaskRegistry} from './ScheduledTaskRegistry';

import {scheduledTaskService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';


const store = createStore(tasksReducer, {tasks: [], ready: false});

AJS.toInit(() => {
    fixStyle();

    scheduledTaskService
        .getAllTasks()
        .then(scripts => store.dispatch(TaskActionCreators.loadTasks(scripts)));

    ReactDOM.render(
        <Provider store={store}>
            <ScheduledTaskRegistry/>
        </Provider>,
        document.getElementById('react-content')
    );
});
