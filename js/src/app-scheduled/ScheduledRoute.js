//@flow
import React, {type ComponentType} from 'react';

import {combineReducers, createStore} from 'redux';
import {Provider} from 'react-redux';
import {Switch, Route} from 'react-router-dom';

import {ScheduledTask} from './ScheduledTask';

import {scheduledTaskService, watcherService} from '../service/services';

import {ItemActionCreators, itemsReducer, readinessReducer, watchesReducer} from '../common/redux';
import {ConnectedScriptPage} from '../common/script-list/ConnectedScriptPage';
import {withRoot} from '../common/script-list/breadcrumbs';

import './ScheduledTaskRegistry.less';
import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {Loader} from '../common/ak/Loader';


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
