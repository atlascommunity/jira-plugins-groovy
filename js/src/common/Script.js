import PropTypes from 'prop-types';
import React from 'react';

import Avatar from '@atlaskit/avatar';
import Button, {ButtonGroup} from '@atlaskit/button';
import Spinner from '@atlaskit/spinner';

import CodeIcon from '@atlaskit/icon/glyph/code';
import EditIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';
import BitbucketSourceIcon from '@atlaskit/icon/glyph/bitbucket/source';

import {Editor} from './Editor';

import {ExecutionBar} from '../execution/ExecutionBar';
import {CommonMessages} from '../i18n/common.i18n';

import {executionService} from '../service/services';

import './Script.less';


//todo: maybe display number of failed executions somewhere in title
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
            changelogs: PropTypes.array
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
        executionsReady: false
    };

    componentDidMount() {
        if (!this.props.collapsible) {
            this._fetchExecutions();
        }
    }

    _showCode = () => {
        const {showCode} = this.state;

        if (!showCode) {
            this._fetchExecutions();
        }
        this.setState({ showCode: !showCode });
    };

    _fetchExecutions() {
        this.setState({ executionsReady: false });
        const {script} = this.props;

        executionService
            .getExecutions(script.inline, script.id)
            .then(result => this.setState({executions: result, executionsReady: true}));
    }

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
                source: changelog.diff
            }
        });
    };

    render() {
        const {script, title, children, collapsible, withChangelog, onEdit, onDelete, additionalButtons, headerless} = this.props;
        const {activeSource, showCode, executions, executionsReady} = this.state;

        let codeBlock = null;
        let executionBar = null;

        const isOpen = showCode || !collapsible;

        if (isOpen) {
            let changelog = null;

            if (withChangelog) {
                changelog = (
                    <div className="scriptChangelogs" style={{width: '150px'}}>
                        <div key="current" className="scriptChangelog" onClick={this._switchToCurrent}>
                            <div className="changelogContent">
                                <strong>{CommonMessages.currentVersion}</strong>
                            </div>
                        </div>
                        {script && script.changelogs && script.changelogs.map(changelog =>
                            <div key={changelog.id} className="scriptChangelog" onClick={this._switchToChangelog(changelog)}>
                                <div className="changelogContent">
                                    <div>
                                        <strong>
                                            {changelog.comment}
                                        </strong>
                                    </div>
                                    <div className="changelogSecondary">
                                        {changelog.date}
                                        <div>
                                            {changelog.author.avatarUrl ? <Avatar src={changelog.author.avatarUrl} size="xsmall"/> : null}
                                            {' '}{changelog.author.displayName}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                );
            }

            codeBlock = (
                <div className="flex-row editor">
                    {changelog}
                    <div className="flex-grow" style={{overflow: 'hidden'}}>
                        <Editor
                            mode={activeSource.type}

                            readOnly={true}
                            value={activeSource.id === 'current' ? script.scriptBody : activeSource.source}
                        />
                    </div>
                </div>
            );

            if (executions || !executionsReady) {
                executionBar = (
                    <div className="executions">
                        {executionsReady && <ExecutionBar executions={executions}/>}
                        {!executionsReady && <Spinner size="small"/>}
                    </div>
                );
            }
        }

        return (
            <div className={`scriptRow ${!isOpen ? 'collapsed' : ''}`}>
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
                                    </Button>}
                                {onEdit &&
                                    <Button
                                        key="edit-button"
                                        appearance="subtle"
                                        iconBefore={<EditIcon label=""/>}

                                        onClick={onEdit}
                                    />}
                                {onDelete &&
                                    <Button
                                        key="delete-button"
                                        appearance="subtle"
                                        iconBefore={<TrashIcon label=""/>}

                                        onClick={onDelete}
                                    />}
                            </ButtonGroup>
                            {additionalButtons}
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
