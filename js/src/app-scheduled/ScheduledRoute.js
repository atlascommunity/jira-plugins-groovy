//@flow
import React from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import {ScheduledTask} from './ScheduledTask';
import {ScheduledTaskForm} from './ScheduledTaskForm';

import {scheduledTaskService, watcherService} from '../service/services';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {ConnectedScriptPage, NotFoundPage, ItemViewPage} from '../common/script-list';
import {Loader} from '../common/ak';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';

import './ScheduledTaskRegistry.less';


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
                <Loader>
                    <Switch>
                        <Route path="/scheduled/" exact={true}>
                            {() =>
                                <ConnectedScriptPage
                                    ScriptComponent={ScheduledTask}
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
                            }
                        </Route>
                        <Route path="/scheduled/create" exact={true}>
                            {() =>
                                <ScheduledTaskForm id={null} isNew={true}/>
                            }
                        </Route>
                        <Route path="/scheduled/:id/edit" exact={true}>
                            {({match}) =>
                                <ScheduledTaskForm id={parseInt(match.params.id, 10)} isNew={false}/>
                            }
                        </Route>
                        <Route path="/scheduled/:id/view" exact={true}>
                            {({match}) =>
                                <ItemViewPage
                                    id={parseInt(match.params.id, 10)}

                                    ScriptComponent={ScheduledTask}
                                    deleteCallback={scheduledTaskService.doDelete}
                                    i18n={{
                                        deleteDialogTitle: ScheduledTaskMessages.deleteTask,
                                        parentName: 'Scheduled tasks'
                                    }}
                                    parentLocation="/scheduled/"
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
