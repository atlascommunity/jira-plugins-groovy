import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Message from 'aui-react/lib/AUIMessage';

import Avatar from '@atlaskit/avatar';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Lozenge from '@atlaskit/lozenge';
import Button from '@atlaskit/button';
import InlineDialog from '@atlaskit/inline-dialog';
import {ToggleStateless} from '@atlaskit/toggle';

import ErrorIcon from '@atlaskit/icon/glyph/error';
import InfoIcon from '@atlaskit/icon/glyph/info';
import {colors} from '@atlaskit/theme';

import {ScheduledTaskDialog} from './ScheduledTaskDialog';
import {types} from './types';
import {TaskActionCreators} from './scheduled.reducer';

import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {FieldMessages, TitleMessages} from '../i18n/common.i18n';
import {Script} from '../common/Script';

import './ScheduledTaskRegistry.less';
import {scheduledTaskService} from '../service/services';
import {StaticField} from '../common/StaticField';


function getOutcomeLozengeAppearance(outcome) {
    switch(outcome) {
        case 'SUCCESS':
            return 'success';
        case 'ABORTED':
            return 'moved';
        case 'UNAVAILABLE':
        case 'FAILED':
            return 'removed';
        case 'NOT_RAN':
            return '';
        default:
            return '';
    }
}

@connect(
    status => {
        return {
            tasks: status.tasks,
            ready: status.ready
        };
    }
)
export class ScheduledTaskRegistry extends React.Component {
    static propTypes = {
        tasks: PropTypes.arrayOf(PropTypes.object.isRequired), //todo: shape
        ready: PropTypes.bool.isRequired
    };

    state = {
        dialogProps: null
    };

    _triggerDialog = (isNew, id) => () => this.setState({ dialogProps: {isNew, id} });

    _closeDialog = () => this.setState({ dialogProps: null });

    render() {
        const {tasks, ready} = this.props;
        const {dialogProps} = this.state;

        if (!ready) {
            return <div className="aui-icon aui-icon-wait"/>;
        }

        //todo
        return (
            <Page>
                <PageHeader
                    actions={
                        <Button
                            appearance="primary"
                            onClick={this._triggerDialog(true)}
                        >
                            {ScheduledTaskMessages.addTask}
                        </Button>
                    }
                >
                    {TitleMessages.scheduled}
                </PageHeader>
                <div className="ScriptList page-content">
                    {!!tasks.length && tasks.map(task =>
                        <ScheduledTask key={task.id} task={task} onEdit={this._triggerDialog(false, task.id)}/>
                    )}
                    {!tasks.length && <Message type="info">{ScheduledTaskMessages.noTasks}</Message>}
                </div>
                {dialogProps && <ScheduledTaskDialog {...dialogProps} onClose={this._closeDialog}/>}
            </Page>
        );
    }
}

//todo: not all tasks will have scripts
//todo: maybe add changelog for all fields
//todo: i18n
@connect(null, TaskActionCreators)
class ScheduledTask extends React.Component {
    static propTypes = {
        task: PropTypes.object.isRequired, //todo: shape
        onEdit: PropTypes.func.isRequired,
        updateTask: PropTypes.func.isRequired,
        deleteTask: PropTypes.func.isRequired
    };

    state = {
        showStatusInfo: false
    };

    _delete = () => {
        const {task} = this.props;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${task.name}"?`)) {
            scheduledTaskService
                .doDelete(task.id)
                .then(() => this.props.deleteTask(task.id));
        }
    };

    _showStatusInfo = () => this.setState({ showStatusInfo: true });

    _hideStatusInfo = () => this.setState({ showStatusInfo: false });

    _toggleEnabled = () => {
        const {task, updateTask} = this.props;

        const enabled = !task.enabled;

        scheduledTaskService
            .setEnabled(task.id, enabled)
            .then(() => updateTask({...task, enabled}));
    };

    render() {
        const {task, onEdit} = this.props;
        const {showStatusInfo} = this.state;
        const {lastRunInfo} = task;

        const outcome = lastRunInfo ? lastRunInfo.outcome : 'NOT_RAN';

        const isError = outcome === 'FAILED';
        const lastRun = lastRunInfo ?
            <div className="flex-column">
                <strong>Last run:</strong>
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
                <strong>Next run:</strong>
                <div>{task.nextRunDate || 'unavailable'}</div>
            </div>
            {lastRun}
        </div>;
        let titleEl = (
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
            changelogs: task.changelogs
        } : null;

        return (
            <Script
                withChangelog={true}
                script={script}
                title={titleEl}
                onEdit={onEdit}
                onDelete={this._delete}
            >
                <form className="aui">
                    <div className="field-group">
                        <label>
                            {FieldMessages.schedule}{':'}
                        </label>
                        <StaticField>
                            {task.scheduleExpression}
                        </StaticField>
                    </div>
                    {task.user && <div className="field-group">
                        <label>
                            {ScheduledTaskMessages.runAs}{':'}
                        </label>
                        <StaticField>
                            <Avatar size="xsmall" src={task.user.imgSrc}/>{' '}{task.user.label}
                        </StaticField>
                    </div>}
                    {task.issueJql && <div className="field-group">
                        <label>
                            {FieldMessages.issueJql}{':'}
                        </label>
                        <StaticField>
                            {task.issueJql}
                        </StaticField>
                    </div>}
                    {task.issueWorkflow && task.issueWorkflowAction && <div className="field-group">
                        <label>
                            {FieldMessages.workflowAction}{':'}
                        </label>
                        <StaticField>
                            {task.issueWorkflow.label}{' - '}{task.issueWorkflowAction.label}
                        </StaticField>
                    </div>}
                    {(task.type === 'ISSUE_JQL_TRANSITION') && task.transitionOptions && <div className="field-group">
                        <label>
                            {ScheduledTaskMessages.transitionOptions}{':'}
                        </label>
                        <StaticField>
                            {Object
                                .keys(task.transitionOptions)
                                .filter(key => task.transitionOptions[key])
                                .map(key => ScheduledTaskMessages.transitionOption[key])
                                .join(', ')
                            || 'None'}
                        </StaticField>
                    </div>}
                </form>
            </Script>
        );
    }
}
