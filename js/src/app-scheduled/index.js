//@flow
import ReactDOM from 'react-dom';
import React, {type ComponentType} from 'react';
import {Provider} from 'react-redux';

import {combineReducers, createStore} from 'redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ScheduledTaskDialog} from './ScheduledTaskDialog';
import {ScheduledTask} from './ScheduledTask';

import {scheduledTaskService, watcherService} from '../service/services';
import {fixStyle} from '../common/fixStyle';

import '../flex.less';
import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';

import './ScheduledTaskRegistry.less';
import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import type {FullDialogComponentProps} from '../common/script-list/types';


const store = createStore(
    combineReducers({
        items: itemsReducer,
        isReady: readinessReducer,
        watches: watchesReducer
    })
);

AJS.toInit(() => {
    fixStyle();

    Promise
        .all([scheduledTaskService.getAllTasks(), watcherService.getAllWatches('SCHEDULED_TASK')])
        .then(([scripts, watches]) => store.dispatch(ItemActionCreators.loadItems(scripts, watches)));

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <Provider store={store}>
            <ConnectedScriptPage
                DialogComponent={(ScheduledTaskDialog: ComponentType<FullDialogComponentProps>)}
                ScriptComponent={ScheduledTask}
                i18n={{
                    title: TitleMessages.rest,
                    addItem: ScheduledTaskMessages.addTask,
                    noItems: ScheduledTaskMessages.noTasks,
                    delete: {
                        heading: ScheduledTaskMessages.deleteTask,
                        areYouSure: CommonMessages.confirmDelete
                    }
                }}
            />
        </Provider>,
        element
    );
});
