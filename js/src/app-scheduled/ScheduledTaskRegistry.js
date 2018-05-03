import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Button from '@atlaskit/button';

import {ScheduledTaskDialog} from './ScheduledTaskDialog';
import {ScheduledTask} from './ScheduledTask';

import {LoadingSpinner} from '../common/ak/LoadingSpinner';
import {InfoMessage} from '../common/ak/messages';

import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {TitleMessages} from '../i18n/common.i18n';

import './ScheduledTaskRegistry.less';


@connect(
    state => {
        return {
            tasks: state.items,
            ready: state.ready
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
            return <LoadingSpinner/>;
        }

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
                    {!tasks.length && <InfoMessage title={ScheduledTaskMessages.noTasks}/>}
                </div>
                {dialogProps && <ScheduledTaskDialog {...dialogProps} onClose={this._closeDialog}/>}
            </Page>
        );
    }
}
