import React from 'react';

import PropTypes from 'prop-types';

import ErrorIcon from '@atlaskit/icon/glyph/error';
import EditorSuccessIcon from '@atlaskit/icon/glyph/editor/success';
import Tooltip from '@atlaskit/tooltip';
import {colors} from '@atlaskit/theme';

import './ExecutionBar.less';
import {ExecutionDialog} from './ExecutionDialog';


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

    render() {
        const execution = this.props.execution;

        return (
            <Tooltip
                content={`${execution.date} - ${execution.time} ms`}
                onClick={this.props.onClick}
            >
                <div className="executionItem">
                    {execution.success ?
                        <EditorSuccessIcon label="success" size="small" primaryColor={colors.G300}/> :
                        <ErrorIcon label="error" size="small" primaryColor={colors.R300}/>
                    }
                </div>
            </Tooltip>
        );
    }
}
