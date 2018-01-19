import PropTypes from 'prop-types';
import React from 'react';

import Avatar from 'aui-react/lib/AUIAvatar';
import Button from 'aui-react/lib/AUIButton';
import Icon from 'aui-react/lib/AUIIcon';

import {Editor} from './Editor';

import {ExecutionBar} from '../execution/ExecutionBar';
import {CommonMessages} from '../i18n/common.i18n';

import {executionService} from '../service/services';

import './Script.less';


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
        }).isRequired,
        onEdit: PropTypes.func,
        onDelete: PropTypes.func,

        collapsible: PropTypes.bool,
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
        const {script, title, children, collapsible, withChangelog, onEdit, onDelete, additionalButtons} = this.props;
        const {activeSource, showCode, executions, executionsReady} = this.state;

        let codeBlock = null;
        let executionBar = null;

        if (showCode || !collapsible) {
            let changelog = null;

            if (withChangelog) {
                changelog = (
                    <div className="scriptChangelogs" style={{width: '150px'}}>
                        <div key="current" className="scriptChangelog" onClick={this._switchToCurrent}>
                            <strong>{CommonMessages.currentVersion}</strong>
                        </div>
                        {script.changelogs && script.changelogs.map(changelog =>
                            <div key={changelog.id} className="scriptChangelog" onClick={this._switchToChangelog(changelog)}>
                                <div>
                                    <Icon icon="devtools-commit"/>
                                    <strong>
                                        {changelog.comment}
                                    </strong>
                                </div>
                                <div>
                                    {changelog.date}
                                </div>
                                <div>
                                    {changelog.author.avatarUrl ? <Avatar src={changelog.author.avatarUrl} size="xsmall"/> : null}
                                    {' '}{changelog.author.displayName}
                                </div>
                            </div>
                        )}
                    </div>
                );
            }

            codeBlock = (
                <div className="flex-row editor">
                    {changelog}
                    <div className="flex-grow">
                        <Editor
                            mode={activeSource.type}

                            readOnly={true}
                            value={activeSource.id === 'current' ? script.scriptBody : activeSource.source}
                        />
                    </div>
                </div>
            );

            executionBar = (
                <div className="flex-none full-width executions">
                    {executionsReady && <ExecutionBar executions={executions}/>}
                    {!executionsReady && <div className="aui-icon aui-icon-wait"/>}
                </div>
            );
        }

        return (
            <div key={script.id} className="scriptRow">
                <div className="flex-row title">
                    {title ?
                        <div className="flex-grow">
                            {title}
                        </div>
                        :
                        <div className="flex-grow">
                            <Icon icon="file-code"/>{' '}
                            <strong>{script.name}</strong>
                        </div>
                    }
                    <div className="flex-none">
                        {collapsible && <Button type="subtle" icon={showCode ? 'arrows-up' : 'arrows-down'} onClick={this._showCode}>{CommonMessages.showCode}</Button>}
                        {onEdit && <Button key="edit-button" type="subtle" icon="edit" onClick={onEdit}>{CommonMessages.edit}</Button>}
                        {onDelete && <Button key="delete-button" type="subtle" icon="delete" onClick={onDelete}>{CommonMessages.delete}</Button>}
                        {additionalButtons}
                    </div>
                </div>
                <div className="children">
                    {children}
                </div>
                {codeBlock}
                {executionBar}
            </div>
        );
    }
}
