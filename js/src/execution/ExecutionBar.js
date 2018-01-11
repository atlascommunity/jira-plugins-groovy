import React from 'react';

import PropTypes from 'prop-types';

import Icon from 'aui-react/lib/AUIIcon';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import './ExecutionBar.less';
import {ExecutionDialog} from './ExecutionDialog';


//todo: execution details
export class ExecutionBar extends React.Component {
    static propTypes = {
        executions: PropTypes.array.isRequired
    };

    state = {
        displayedExecution: null
    };

    _showExecution = (execution) => () => this.setState({ displayedExecution: execution });

    _hideExecution = () => this.setState({ displayedExecution: null });

    render() {
        const {displayedExecution} = this.state;

        return (
            <div className="executionBar">
                {this.props.executions.map(execution =>
                    <ExecutionItem key={execution.id} execution={execution} onClick={this._showExecution(execution)}/>
                )}
                {displayedExecution && <ExecutionDialog onClose={this._hideExecution} execution={displayedExecution}/>}
            </div>
        );
    }
}

class ExecutionItem extends React.Component {
    static propTypes = {
        execution: PropTypes.object.isRequired,
        onClick: PropTypes.func
    };

    state = {
        hovered: false
    };

    _addTooltip = (el) => {
        AJS.$(el).tooltip({gravity: 'n'});
    };

    render() {
        const execution = this.props.execution;

        return (
            <div
                key={execution.id}
                className="executionItem"
                title={`${execution.date} - ${execution.time} ms`}
                ref={this._addTooltip}
                onClick={this.props.onClick}
            >
                <Icon
                    icon={execution.success ? 'approve' : 'error'}
                    style={{ color: execution.success ? 'green' : 'red'}}
                />
            </div>
        );
    }
}
