//@flow
import React from 'react';

import ErrorIcon from '@atlaskit/icon/glyph/error';
import EditorSuccessIcon from '@atlaskit/icon/glyph/editor/success';
import Tooltip from '@atlaskit/tooltip';
import {colors} from '@atlaskit/theme';

import {ExecutionDialog} from './ExecutionDialog';
import type {ExecutionType} from './types';

import type {VoidCallback} from '../types';

import {FieldMessages} from '../../i18n/common.i18n';

import './ExecutionBar.less';


type ExecutionBarProps = {
    executions: $ReadOnlyArray<ExecutionType>
};

type ExecutionBarState = {
    displayedExecution?: ?ExecutionType
};

export class ExecutionBar extends React.PureComponent<ExecutionBarProps, ExecutionBarState> {
    state = {
        displayedExecution: null
    };

    _showExecution = (execution: ExecutionType) => () => this.setState({ displayedExecution: execution });

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

type ExecutionItemProps = {
    execution: ExecutionType,
    onClick: VoidCallback
};

class ExecutionItem extends React.PureComponent<ExecutionItemProps> {
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
