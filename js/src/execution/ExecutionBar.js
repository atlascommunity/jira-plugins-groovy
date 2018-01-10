import React from 'react';

import PropTypes from 'prop-types';

import Icon from 'aui-react/lib/AUIIcon';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import './ExecutionBar.less';


//todo: execution details
export class ExecutionBar extends React.Component {
    static propTypes = {
        executions: PropTypes.array.isRequired
    };

    render() {
        return (
            <div className="executionBar">
                {this.props.executions.map(execution =>
                    <ExecutionItem key={execution.id} execution={execution}/>
                )}
            </div>
        );
    }
}

class ExecutionItem extends React.Component {
    static propTypes = {
        execution: PropTypes.object.isRequired
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
                title={`${execution.date} - ${execution.time} ms; ${execution.error ? execution.error : ''}`}
                ref={this._addTooltip}
            >
                <Icon
                    icon={execution.success ? 'approve' : 'error'}
                    style={{ color: execution.success ? 'green' : 'red'}}
                />
            </div>
        );
    }
}
