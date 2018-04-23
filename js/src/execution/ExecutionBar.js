import React from 'react';

import PropTypes from 'prop-types';

import ErrorIcon from '@atlaskit/icon/glyph/error';
import EditorSuccessIcon from '@atlaskit/icon/glyph/editor/success';
import Tooltip from '@atlaskit/tooltip';
import {colors} from '@atlaskit/theme';

import {ExecutionDialog} from './ExecutionDialog';

import {FieldMessages} from '../i18n/common.i18n';

import './ExecutionBar.less';


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
        const params = execution.extraParams || {};

        return (
            <Tooltip
                content={
                    <div className="flex-column">
                        <strong>
                            {execution.extraParams.type || ''}
                        </strong>
                        {params.issue && <span>{FieldMessages.issue}{': '}{params.issue}</span>}
                        <span>
                            {execution.date}{' - '}{execution.time}{' ms'}
                        </span>
                    </div>
                }
            >
                <div className="executionItem" onClick={this.props.onClick}>
                    {execution.success ?
                        <EditorSuccessIcon label="success" size="small" primaryColor={colors.G300}/> :
                        <ErrorIcon label="error" size="small" primaryColor={colors.R300}/>
                    }
                </div>
            </Tooltip>
        );
    }
}
