import React from 'react';
import PropTypes from 'prop-types';

import ModalDialog from '@atlaskit/modal-dialog';
import Spinner from '@atlaskit/spinner';

import {ScheduledTaskMessages} from '../i18n/scheduled.i18n';
import {CommonMessages} from '../i18n/common.i18n';
import {scheduledTaskService} from '../service/services';
import {ConsoleMessages} from '../i18n/console.i18n';


export class RunNowDialog extends React.PureComponent {
    static propTypes = {
        task: PropTypes.object.isRequired,
        onClose: PropTypes.func.isRequired
    };

    state = {
        running: false,
        result: null,
        error: ''
    };

    _onClose = () => {
        const {running} = this.state;
        const {onClose} = this.props;

        if (!running) {
            onClose();
        }
    };

    _runNow = () => {
        this.setState({ running: true });

        scheduledTaskService
            .runNow(this.props.task.id)
            .then(
                (result) => this.setState({
                    running: false,
                    result
                }),
                error => {
                    this.setState({ running: false });
                    throw error;
                }
            );
    };

    render() {
        const {task} = this.props;
        const {running, result} = this.state;

        if (result) {
            return (
                <ModalDialog
                    width="small"
                    scrollBehavior="outside"
                    heading={`${CommonMessages.completed}: ${task.name}`}

                    actions={[
                        {
                            text: CommonMessages.close,
                            onClick: this._onClose
                        }
                    ]}
                >
                    {ConsoleMessages.executedIn(result.time)}
                    <br/>
                    <strong>{result.runOutcome}{result.message && ':'}</strong>
                    {result.message}
                </ModalDialog>
            );
        }

        const actions = [
            {
                text: ScheduledTaskMessages.runNow,
                onClick: this._runNow,
                isDisabled: running
            },
            {
                text: CommonMessages.cancel,
                onClick: this._onClose,
                isDisabled: running
            }
        ];

        return (
            <ModalDialog
                width="small"
                scrollBehavior="outside"
                heading={`${ScheduledTaskMessages.runNow}: ${task.name}`}

                actions={actions}
                onClose={this._onClose}
            >
                {running && <div className="flex-horizontal-middle"><Spinner size="medium"/></div>}
                {!running && ScheduledTaskMessages.runNowConfirm(task.name)}
            </ModalDialog>
        );
    }
}
