//@flow
import * as React from 'react';

import Button, {ButtonGroup} from '@atlaskit/button';
import Spinner from '@atlaskit/spinner';
import Badge from '@atlaskit/badge';

import CodeIcon from '@atlaskit/icon/glyph/code';
import EditIcon from '@atlaskit/icon/glyph/edit-filled';
import BitbucketSourceIcon from '@atlaskit/icon/glyph/bitbucket/source';
import RecentIcon from '@atlaskit/icon/glyph/recent';

import type {ScriptType, ExecutionType, ChangelogType} from './types';

import {Changelog} from './Changelog';
import {ExecutionBar} from './ExecutionBar';

import type {VoidCallback} from '../types';

import {Editor} from '../editor/Editor';

import {CommonMessages} from '../../i18n/common.i18n';

import {executionService} from '../../service/services';


type ScriptProps = {
    withChangelog: boolean,
    collapsible: boolean,
    headerless: boolean,

    script: ScriptType,
    template?: {
        body: string
    },

    onEdit?: VoidCallback,
    onDelete?: VoidCallback,

    title?: React.Node,
    children?: React.Node,
    additionalButtons?: Array<React.Element<any>>
}

type ScriptState = {
    showCode: boolean,
    activeSource: {
        type: string,
        id: number | string,
        source?: string,
        templateSource?: string
    },
    executions: Array<ExecutionType>,
    onlyLastExecutions: boolean,
    executionsReady: boolean
};

export class Script extends React.Component<ScriptProps, ScriptState> {
    static defaultProps = {
        collapsible: true,
        withChangelog: false,
        headerless: false
    };

    state = {
        showCode: false,
        activeSource: {
            type: 'groovy',
            id: 'current'
        },
        executions: [],
        onlyLastExecutions: true,
        executionsReady: false
    };

    componentDidMount() {
        if (!this.props.collapsible) {
            this._fetchExecutions();
        }
    }

    _showCode = () => {
        const {showCode} = this.state;

        this.setState({ showCode: !showCode, onlyLastExecutions: true }, () => {
            if (this.state.showCode) {
                this._fetchExecutions();
            }
        });
    };

    _showAllExecutions = () => this.setState({ onlyLastExecutions: false }, this._fetchExecutions);

    _fetchExecutions = () => {
        this.setState({ executionsReady: false });
        const {script} = this.props;
        const {onlyLastExecutions} = this.state;

        if (script) {
            executionService
                .getExecutions(script.inline, script.id, onlyLastExecutions)
                .then(result => this.setState({executions: result, executionsReady: true}));
        }
    };

    _switchToCurrent = () => {
        this.setState({
            activeSource: {
                type: 'groovy',
                id: 'current'
            }
        });
    };

    _switchToChangelog = (changelog: ChangelogType) => () => {
        this.setState({
            activeSource: {
                type: 'diff',
                id: changelog.id,
                source: changelog.diff,
                templateSource: changelog.templateDiff
            }
        });
    };

    render(): React.Node {
        const {script, template, title, children, collapsible, withChangelog, onEdit, additionalButtons, headerless} = this.props;
        const {activeSource, showCode, executions, executionsReady, onlyLastExecutions} = this.state;

        let codeBlock : React.Node = null;
        let templateBlock : React.Node = null;
        let executionBar : React.Node = null;

        const isOpen : boolean = showCode || !collapsible;

        if (isOpen) {
            if (template) {
                templateBlock = (
                    <div style={{overflow: 'hidden'}}>
                        <Editor
                            readOnly={true}
                            mode={activeSource.id === 'current' ? 'velocity' : 'diff'}
                            value={activeSource.id === 'current' ? template.body : activeSource.templateSource}
                        />
                    </div>
                );
            }

            codeBlock = (
                <div className="flex-row editor">
                    {withChangelog && script.changelogs &&
                    <Changelog
                        changelogs={script.changelogs}
                        switchToChangelog={this._switchToChangelog}
                        switchToCurrent={this._switchToCurrent}
                    />
                    }
                    <div className="flex-grow flex-column">
                        <div style={{overflow: 'hidden'}}>
                            <Editor
                                readOnly={true}
                                mode={activeSource.id === 'current' ? 'groovy' : 'diff'}
                                value={activeSource.id === 'current' ? script && script.scriptBody : activeSource.source}
                            />
                        </div>
                        {templateBlock}
                    </div>
                </div>
            );

            if ((executions && executions.length) || !executionsReady) {
                executionBar = (
                    <div className="executions">
                        {executionsReady &&
                        <div className="flex-row">
                            <ExecutionBar executions={executions}/>
                            {onlyLastExecutions && <div className="flex-grow"/>}
                            {onlyLastExecutions &&
                            <div>
                                <Button
                                    appearance="subtle"
                                    iconBefore={<RecentIcon label=""/>}
                                    spacing="compact"

                                    onClick={this._showAllExecutions}
                                >
                                    {CommonMessages.showAll}
                                </Button>
                            </div>}
                        </div>
                        }
                        {!executionsReady && <Spinner size="small"/>}
                    </div>
                );
            }
        }

        const buttons : Array<React.Element<any>> = [];

        if (collapsible && script) {
            buttons.push(
                <Button
                    key="toggleCode"

                    appearance="subtle"
                    iconBefore={<BitbucketSourceIcon label=""/>}
                    onClick={this._showCode}
                >
                    {showCode ? CommonMessages.hideCode : CommonMessages.showCode}
                </Button>
            );
        }

        if (onEdit) {
            buttons.push(
                <Button
                    key="edit-button"
                    appearance="subtle"
                    iconBefore={<EditIcon label=""/>}

                    onClick={onEdit}
                />
            );
        }

        if (additionalButtons) {
            buttons.push(...additionalButtons);
        }

        return (
            <div className={`scriptRow ${!isOpen ? 'collapsed' : ''} ${template ? 'withTemplate' : ''}`}>
                {!headerless &&
                <div className="flex-row title">
                    {title ?
                        <div className="flex-grow flex-vertical-middle">
                            {title}
                        </div>
                        :
                        <div className="flex-grow flex-row">
                            <div className="flex-vertical-middle flex-none">
                                <CodeIcon label=""/>
                            </div>
                            {' '}
                            <div className="flex-vertical-middle flex-grow">
                                <h3 title={script && script.name}>
                                    {script && script.name}
                                </h3>
                            </div>
                            {script && !!script.errorCount &&
                            <div className="flex-vertical-middle flex-none errorCount">
                                <div>
                                    <Badge max={99} value={script.errorCount} appearance="important"/>
                                </div>
                            </div>
                            }
                        </div>
                    }
                    <div className="flex-none flex-row">
                        <ButtonGroup>
                            {buttons}
                        </ButtonGroup>
                    </div>
                </div>
                }
                <div className="children">
                    {children}
                </div>
                {isOpen && <div className="ScriptBody">
                    {codeBlock}
                    {executionBar}
                </div>}
            </div>
        );
    }
}
