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
        editable: PropTypes.bool.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired,

        title: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.node
        ]),
        children: PropTypes.oneOfType([
            PropTypes.arrayOf(PropTypes.node),
            PropTypes.node
        ])
    };

    state = {
        showCode: false,
        activeSource: {
            type: 'groovy',
            id: 'current'
        },
        executions: []
    };

    _showCode = () => {
        const script = this.props.script;
        if (!this.state.showCode) {
            executionService
                .getExecutions(script.inline, script.id)
                .then(result => this.setState({executions: result}));
        }
        this.setState({ showCode: !this.state.showCode });
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
                source: changelog.diff
            }
        });
    };

    render() {
        const {script, title, children} = this.props;
        const activeSource = this.state.activeSource;

        let codeBlock = null;
        let executionBar = null;

        if (this.state.showCode) {
            let changelog = null;

            if (this.props.withChangelog) {
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
                <div className="flex-row">
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
                <div className="flex-none full-width">
                    <ExecutionBar executions={this.state.executions}/>
                </div>
            );
        }

        return (
            <div key={script.id} className="scriptRow">
                <div className="flex-row">
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
                        <Button type="subtle" icon={this.state.showCode ? 'arrows-up' : 'arrows-down'} onClick={this._showCode}>{CommonMessages.showCode}</Button>
                        {this.props.editable ? [
                            <Button key="edit-button" type="subtle" icon="edit" onClick={this.props.onEdit}>{CommonMessages.edit}</Button>,
                            <Button key="delete-button" type="subtle" icon="delete" onClick={this.props.onDelete}>{CommonMessages.delete}</Button>
                        ] : null}
                    </div>
                </div>
                {children}
                {codeBlock}
                {executionBar}
            </div>
        );
    }
}
