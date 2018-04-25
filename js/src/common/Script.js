//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import reactStringReplace from 'react-string-replace';

import Avatar from '@atlaskit/avatar';
import Button, {ButtonGroup} from '@atlaskit/button';
import Spinner from '@atlaskit/spinner';
import Badge from '@atlaskit/badge';
import Tooltip from '@atlaskit/tooltip';
import {Label} from '@atlaskit/field-base';

import CodeIcon from '@atlaskit/icon/glyph/code';
import EditIcon from '@atlaskit/icon/glyph/edit-filled';
import BitbucketSourceIcon from '@atlaskit/icon/glyph/bitbucket/source';
import RecentIcon from '@atlaskit/icon/glyph/recent';

import {Editor} from './editor/Editor';

import {ExecutionBar} from '../execution/ExecutionBar';
import {CommonMessages} from '../i18n/common.i18n';

import {executionService} from '../service/services';

import {getBaseUrl} from '../service/ajaxHelper';

import './Script.less';


type ScriptParam = {
    label: string,
    value: React.Node
}

type ScriptParametersProps = {
    params: Array<ScriptParam>
};

export class ScriptParameters extends React.PureComponent<ScriptParametersProps> {
    render() {
        const {params} = this.props;

        return (
            <div className="scriptParams">
                {params.map((param, i) =>
                    <div className="item" key={i}>
                        <div className="label">
                            <Label label={`${param.label}:`} isFirstChild={true}/>
                        </div>
                        <div className="value">
                            {param.value}
                        </div>
                    </div>
                )}
            </div>
        );
    }
}

type ChangelogType = any; //todo
type ExecutionType = any; //todo

type ScriptType = {
    id: number | string,
    name: string,
    scriptBody?: string,
    inline?: boolean,
    changelogs?: Array<ChangelogType>,
    errorCount?: number
};

type VoidCallback = () => void;

type ScriptProps = {
    withChangelog: boolean,
    collapsible: boolean,
    headerless: boolean,

    script?: ScriptType, //todo: maybe make script non-optional
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
        id: string,
        source?: string,
        templateSource?: string
    },
    executions: Array<ExecutionType>,
    onlyLastExecutions: boolean,
    executionsReady: boolean
};


//todo: common component for displaying script parameters, maybe add prop for parameters
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

    _switchToChangelog = (changelog : ChangelogType) => () => {
        this.setState({
            activeSource: {
                type: 'diff',
                id: changelog.id,
                source: changelog.diff,
                templateSource: changelog.templateDiff
            }
        });
    };

    render() {
        const {script, template, title, children, collapsible, withChangelog, onEdit, onDelete, additionalButtons, headerless} = this.props;
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
                    {withChangelog && script && script.changelogs &&
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
                                <div className="flex-vertical-middle">
                                    <CodeIcon label=""/>
                                </div>
                                {' '}
                                <div className="flex-vertical-middle">
                                    <h3 title={script && script.name}>
                                        {script && script.name}
                                    </h3>
                                </div>
                                {script && !!script.errorCount &&
                                    <div className="flex-vertical-middle" style={{marginLeft: '5px'}}>
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

function ChangelogComment({text, issueReferences}) {
    if (!(issueReferences && issueReferences.length)) {
        return <p>{text}</p>;
    }

    return (
        <p>
            {reactStringReplace(text, /([A-Z0-9a-z]{1,10}-\d+)/g, (issueKey, i) => {
                const issueReference = issueReferences.find(ref => ref.key === issueKey);

                if (issueReference) {
                    return (
                        <Tooltip
                            key={`${issueKey}-${i}`}
                            tag="span"
                            content={issueReference.summary}
                        >
                            <a href={`${getBaseUrl()}/browse/${issueKey}`}>{issueKey}</a>
                        </Tooltip>
                    );
                } else {
                    return issueKey;
                }
            })}
        </p>
    );
}

type ChangelogProps = {
    changelogs: Array<ChangelogType>,
    switchToCurrent: VoidCallback,
    switchToChangelog: (ChangelogType) => () => void
};

function Changelog({changelogs, switchToCurrent, switchToChangelog} : ChangelogProps) : React.Node {
    return (
        <div className="scriptChangelogs" style={{width: '150px'}}>
            <div key="current" className="scriptChangelog" onClick={switchToCurrent}>
                <div className="changelogContent">
                    <strong>{CommonMessages.currentVersion}</strong>
                </div>
            </div>
            {changelogs && changelogs.map(changelog =>
                <div key={changelog.id} className="scriptChangelog" onClick={switchToChangelog(changelog)}>
                    <div className="changelogContent">
                        <Tooltip content={changelog.author.displayName}>
                            <div className="flex-row">
                                <Avatar src={changelog.author.avatarUrl} size="xsmall" appearance="square" borderColor="transparent"/>
                                <span className="author">
                                    {changelog.author.displayName}
                                </span>
                            </div>
                        </Tooltip>
                        <div className="date">
                            {changelog.date}
                        </div>
                        <div>
                            <ChangelogComment
                                text={changelog.comment}
                                issueReferences={changelog.issueReferences}
                            />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

Changelog.propTypes = {
    changelogs: PropTypes.array,
    switchToCurrent: PropTypes.func,
    switchToChangelog: PropTypes.func
};
