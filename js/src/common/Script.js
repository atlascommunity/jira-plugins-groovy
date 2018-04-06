import PropTypes from 'prop-types';
import React from 'react';

import Avatar from '@atlaskit/avatar';
import Button, {ButtonGroup} from '@atlaskit/button';
import Spinner from '@atlaskit/spinner';
import Badge from '@atlaskit/badge';
import Tooltip from '@atlaskit/tooltip';

import CodeIcon from '@atlaskit/icon/glyph/code';
import EditIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';
import BitbucketSourceIcon from '@atlaskit/icon/glyph/bitbucket/source';
import RecentIcon from '@atlaskit/icon/glyph/recent';

import {Editor} from './editor/Editor';

import {ExecutionBar} from '../execution/ExecutionBar';
import {CommonMessages} from '../i18n/common.i18n';

import {executionService} from '../service/services';

import './Script.less';


//todo: common component for displaying script parameters, maybe add prop for parameters
export class Script extends React.Component {
    static propTypes = {
        withChangelog: PropTypes.bool.isRequired,
        script: PropTypes.shape({
            id: PropTypes.oneOfType([
                PropTypes.number,
                PropTypes.string
            ]),
            name: PropTypes.string,
            scriptBody: PropTypes.string,
            inline: PropTypes.bool,
            changelogs: PropTypes.array,
            errorCount: PropTypes.number
        }),
        template: PropTypes.shape({
            body: PropTypes.string
        }),
        onEdit: PropTypes.func,
        onDelete: PropTypes.func,

        collapsible: PropTypes.bool,
        headerless: PropTypes.bool,

        title: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.node
        ]),
        children: PropTypes.oneOfType([
            PropTypes.arrayOf(PropTypes.node),
            PropTypes.node
        ]),
        additionalButtons: PropTypes.oneOfType([
            PropTypes.arrayOf(PropTypes.node),
            PropTypes.node
        ])
    };

    static defaultProps = {
        collapsible: true
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

        executionService
            .getExecutions(script.inline, script.id, onlyLastExecutions)
            .then(result => this.setState({executions: result, executionsReady: true}));
    };

    _switchToCurrent = () => {
        this.setState({
            activeSource: {
                type: 'groovy',
                id: 'current'
            }
        });
    };

    _switchToChangelog = changelog => () => {
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

        let codeBlock = null;
        let templateBlock = null;
        let executionBar = null;

        const isOpen = showCode || !collapsible;

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
                    {withChangelog &&
                        <Changelog
                            changelogs={script && script.changelogs}
                            switchToChangelog={this._switchToChangelog}
                            switchToCurrent={this._switchToCurrent}
                        />
                    }
                    <div className="flex-grow flex-column">
                        <div style={{overflow: 'hidden'}}>
                            <Editor
                                readOnly={true}
                                mode={activeSource.id === 'current' ? 'groovy' : 'diff'}
                                value={activeSource.id === 'current' ? script.scriptBody : activeSource.source}
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
                                    <h3>
                                        {script && script.name}
                                    </h3>
                                </div>
                                {!!script.errorCount &&
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
                                {collapsible && script &&
                                    <Button
                                        appearance="subtle"
                                        iconBefore={<BitbucketSourceIcon label=""/>}
                                        onClick={this._showCode}
                                    >
                                        {showCode ? CommonMessages.hideCode : CommonMessages.showCode}
                                    </Button>
                                }
                                {onEdit &&
                                    <Button
                                        key="edit-button"
                                        appearance="subtle"
                                        iconBefore={<EditIcon label=""/>}

                                        onClick={onEdit}
                                    />
                                }
                                {onDelete &&
                                    <Button
                                        key="delete-button"
                                        appearance="subtle"
                                        iconBefore={<TrashIcon label=""/>}

                                        onClick={onDelete}
                                    />
                                }
                                {additionalButtons}
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

function Changelog({changelogs, switchToCurrent, switchToChangelog}) {
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
                            <p>
                                {changelog.comment}
                            </p>
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
