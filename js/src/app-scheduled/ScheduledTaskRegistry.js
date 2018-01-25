import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

import {ScheduledTaskDialog} from './ScheduledTaskDialog';

import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {TitleMessages} from '../i18n/common.i18n';
import {Script} from '../common/Script';

import './ScheduledTaskRegistry.less';
import {Tooltipped} from '../common/aui/Tooltipped';


function getOutcomeLozengeClass(outcome) {
    switch(outcome) {
        case 'SUCCESS':
            return 'aui-lozenge-success';
        case 'UNAVAILABLE':
            return 'aui-lozenge-moved';
        case 'ABORTED':
            return 'aui-lozenge-current';
        case 'FAILED':
            return 'aui-lozenge-error';
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
            <div>
                <header className="aui-page-header">
                    <div className="aui-page-header-inner">
                        <div className="aui-page-header-main">
                            <h2>{TitleMessages.scheduled}</h2>
                        </div>
                        <div className="aui-page-header-actions">
                            <Button onClick={this._triggerDialog(true)}>
                                {ScheduledTaskMessages.addTask}
                            </Button>
                        </div>
                    </div>
                </header>
                <div className="ScriptList page-content">
                    {!!tasks.length && tasks.map(task =>
                        <ScheduledTask key={task.id} task={task} onEdit={this._triggerDialog(false, task.id)}/>
                    )}
                    {!tasks.length && <Message type="info">{ScheduledTaskMessages.noTasks}</Message>}
                </div>
                {dialogProps && <ScheduledTaskDialog {...dialogProps} onClose={this._closeDialog}/>}
            </div>
        );
    }
}

class ScheduledTask extends React.Component {
    static propTypes = {
        task: PropTypes.object.isRequired,
        onEdit: PropTypes.func.isRequired//todo: shape
    };

    render() {
        const {task, onEdit} = this.props;
        const {lastRunInfo} = task;

        const outcome = lastRunInfo ? lastRunInfo.outcome : 'NOT_RAN';

        let titleEl = (
            <div className="flex-row">
                <div className="flex-vertical-middle">
                    <Tooltipped title={lastRunInfo && `${lastRunInfo.startDate} - ${lastRunInfo.duration/1000}s`}>
                        <div className={`aui-lozenge aui-lozenge-subtle ${getOutcomeLozengeClass(outcome)}`}>
                            {outcome}
                        </div>
                    </Tooltipped>
                </div>
                <h3 style={{marginTop: '2px', marginLeft: '2px'}}>
                    {' '}
                    {task.name}
                </h3>
            </div>
        );

        return (
            <Script
                withChangelog={true}
                script={{
                    id: task.uuid,
                    name: task.name,
                    scriptBody: task.scriptBody,
                    inline: true,
                    changelogs: task.changelogs
                }}
                title={titleEl}
                onEdit={onEdit}
            >
                {lastRunInfo && lastRunInfo.message &&
                    <Message type={(outcome === 'FAILED') ? 'error' : 'info'}>
                        {lastRunInfo.message}
                    </Message>
                }
                <div>
                    {task.type}
                </div>
            </Script>
        );
    }
}
