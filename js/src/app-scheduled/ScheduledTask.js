//@flow
import * as React from 'react';

import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import Avatar from '@atlaskit/avatar';
import Lozenge from '@atlaskit/lozenge';
import InlineDialog from '@atlaskit/inline-dialog';
import {ToggleStateless} from '@atlaskit/toggle';
import {colors} from '@atlaskit/theme';

import type {Appearances} from '@atlaskit/lozenge/dist/cjs/Lozenge/index';

import ErrorIcon from '@atlaskit/icon/glyph/error';
import InfoIcon from '@atlaskit/icon/glyph/info';

import {RunNowDialog} from './RunNowDialog';
import {types} from './types';

import type {RunOutcomeType, ScheduledTaskType} from './types';

import {ScriptParameters} from '../common/script';

import {scheduledTaskService} from '../service/services';

import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {FieldMessages} from '../i18n/common.i18n';
import {ItemActionCreators, WatchActionCreators} from '../common/redux';

import type {VoidCallback} from '../common/types';
import type {ScriptParam} from '../common/script/ScriptParameters';
import {WatchableScript} from '../common/script/WatchableScript';


function getOutcomeLozengeAppearance(outcome: RunOutcomeType): Appearances {
    switch(outcome) {
        case 'SUCCESS':
            return 'success';
        case 'ABORTED':
            return 'moved';
        case 'UNAVAILABLE':
        case 'FAILED':
            return 'removed';
        case 'NOT_RAN':
            return 'default';
        default:
            return 'default';
    }
}

const ConnectedWatchableScript = connect(
    memoizeOne(
        (state: *): * => {
            return {
                watches: state.watches
            };
        }
    ),
    WatchActionCreators
)(WatchableScript);

type Props = {
    task: ScheduledTaskType,
    onEdit: VoidCallback,
    updateItem: typeof ItemActionCreators.updateItem,
    deleteItem: typeof ItemActionCreators.deleteItem,
};

type State = {
    showStatusInfo: boolean,
    showRunDialog: boolean
};

export class ScheduledTaskInternal extends React.Component<Props, State> {
    state = {
        showStatusInfo: false,
        showRunDialog: false
    };

    _delete = () => {
        const {task} = this.props;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${task.name}"?`)) {
            scheduledTaskService
                .doDelete(task.id)
                .then(() => this.props.deleteItem(task.id));
        }
    };

    _showStatusInfo = () => this.setState({ showStatusInfo: true });

    _hideStatusInfo = () => this.setState({ showStatusInfo: false });

    _toggleEnabled = () => {
        const {task, updateItem} = this.props;

        const enabled = !task.enabled;

        scheduledTaskService
            .setEnabled(task.id, enabled)
            .then(() => updateItem({...task, enabled}));
    };

    _toggleRunNow = () => this.setState((state: State): * => {
        return {
            showRunDialog: !state.showRunDialog
        };
    });

    _getParams = memoizeOne(
        (task: ScheduledTaskType): Array<ScriptParam> => {
            const params = [
                {
                    label: FieldMessages.schedule,
                    value: task.scheduleExpression
                }
            ];

            if (task.user) {
                params.push({
                    label: ScheduledTaskMessages.runAs,
                    value: (
                        <div className="flex-row">
                            <Avatar size="xsmall" appearance="square" src={task.user.imgSrc}/>
                            <div className="flex-vertical-middle">
                                {' '}{task.user.label}
                            </div>
                        </div>
                    )
                });
            }

            if (task.issueJql) {
                params.push({
                    label: FieldMessages.issueJql,
                    value: task.issueJql
                });
            }

            if (task.issueWorkflow && task.issueWorkflowAction) {
                params.push({
                    label: FieldMessages.workflowAction,
                    value: `${task.issueWorkflow.label}' - '${task.issueWorkflowAction.label}`
                });
            }

            if ((task.type === 'ISSUE_JQL_TRANSITION') && task.transitionOptions) {
                params.push({
                    label: ScheduledTaskMessages.transitionOptions,
                    value: Object
                        .keys(task.transitionOptions)
                        .filter(key => task.transitionOptions[key])
                        //$FlowFixMe
                        .map(key => ScheduledTaskMessages.transitionOption[key])
                        .join(', ') || 'None'
                });
            }

            return params;
        }
    );

    render(): React.Node {
        const {task, onEdit} = this.props;
        const {showStatusInfo, showRunDialog} = this.state;
        const {lastRunInfo} = task;

        const outcome = lastRunInfo ? lastRunInfo.outcome : 'NOT_RAN';

        const isError = outcome === 'FAILED';
        const lastRun = lastRunInfo ?
            <div className="flex-column">
                <strong>{ScheduledTaskMessages.lastRun}{':'}</strong>
                <div>
                    {lastRunInfo.startDate}{' - '}{lastRunInfo.duration/1000}{'s'}
                </div>
                {lastRunInfo.message && <div className="flex-row">
                    <div style={{color: isError ? colors.R400 : colors.P300}}>
                        {isError ? <ErrorIcon size={'medium'}/> : <InfoIcon size={'medium'}/>}
                    </div>
                    <div className="TaskRunMessage">
                        {lastRunInfo.message}
                    </div>
                </div>}
            </div> : '';

        const popup = <div className="flex-column">
            <div>
                <strong>{ScheduledTaskMessages.nextRun}{':'}</strong>
                <div>{task.nextRunDate || 'unavailable'}</div>
            </div>
            {lastRun}
        </div>;
        let titleEl: React.Node = (
            <div className="flex-row space-between">
                <div className="flex-vertical-middle">
                    <ToggleStateless
                        isChecked={task.enabled}
                        onChange={this._toggleEnabled}
                    />
                </div>
                <div className="flex-vertical-middle">
                    <span style={{marginTop: '2px', marginLeft: '2px'}}>
                        {types[task.type].name}{': '}
                        <strong>{task.name}</strong>
                    </span>
                </div>
                <div className="flex-vertical-middle">
                    <InlineDialog
                        content={popup}
                        isOpen={showStatusInfo}
                        respondsTo="hover"
                        alignment="bottom center"
                    >
                        <div
                            className="flex-vertical-middle"
                            onMouseEnter={this._showStatusInfo}
                            onMouseLeave={this._hideStatusInfo}
                        >
                            <Lozenge appearance={getOutcomeLozengeAppearance(outcome)} isBold={true}>
                                {outcome}
                            </Lozenge>
                        </div>
                    </InlineDialog>
                </div>
            </div>
        );

        const script = (task.type !== 'ISSUE_JQL_TRANSITION') ? {
            id: task.uuid,
            name: task.name,
            scriptBody: task.scriptBody,
            inline: true,
            changelogs: task.changelogs,
            description: task.description
        } : null;

        return (
            <ConnectedWatchableScript
                entityId={task.id}
                entityType="SCHEDULED_TASK"

                withChangelog={true}
                script={script}
                title={titleEl}
                onEdit={onEdit}
                onDelete={this._delete}

                dropdownItems={[
                    {
                        label: ScheduledTaskMessages.runNow,
                        onClick: this._toggleRunNow
                    }
                ]}
            >
                {task.type === 'ISSUE_JQL_TRANSITION' && task.description &&
                    <div className="scriptDescription">
                        {task.description}
                    </div>
                }
                <ScriptParameters
                    params={this._getParams(task)}
                />
                {showRunDialog && <RunNowDialog task={task} onClose={this._toggleRunNow}/>}
            </ConnectedWatchableScript>
        );
    }
}

export const ScheduledTask = connect(
    null,
    {
        updateItem: ItemActionCreators.updateItem,
        deleteItem: ItemActionCreators.deleteItem
    }
)(ScheduledTaskInternal);
