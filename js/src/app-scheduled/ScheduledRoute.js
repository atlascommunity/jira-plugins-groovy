//@flow
import React, {type ComponentType} from 'react';
import {Provider} from 'react-redux';

import {combineReducers, createStore} from 'redux';

import {ScheduledTaskDialog} from './ScheduledTaskDialog';
import {ScheduledTask} from './ScheduledTask';

import {scheduledTaskService, watcherService} from '../service/services';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';
import type {FullDialogComponentProps} from '../common/script-list/types';
import {withRoot} from '../common/script-list/breadcrumbs';

import './ScheduledTaskRegistry.less';
import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';


export class ScheduledRoute extends React.PureComponent<{}> {
    store = createStore(
        combineReducers({
            items: itemsReducer,
            isReady: readinessReducer,
            watches: watchesReducer
        })
    );

    componentDidMount() {
        Promise
            .all([scheduledTaskService.getAllTasks(), watcherService.getAllWatches('SCHEDULED_TASK')])
            .then(([scripts, watches]) => this.store.dispatch(ItemActionCreators.loadItems(scripts, watches)));
    }

    render() {
        return (
            <Provider store={this.store}>
                <ConnectedScriptPage
                    DialogComponent={(ScheduledTaskDialog: ComponentType<FullDialogComponentProps>)}
                    ScriptComponent={ScheduledTask}
                    breadcrumbs={withRoot([])}
                    i18n={{
                        title: TitleMessages.scheduled,
                        addItem: ScheduledTaskMessages.addTask,
                        noItems: ScheduledTaskMessages.noTasks,
                        delete: {
                            heading: ScheduledTaskMessages.deleteTask,
                            areYouSure: CommonMessages.confirmDelete
                        }
                    }}
                />
            </Provider>
        );
    }
}
