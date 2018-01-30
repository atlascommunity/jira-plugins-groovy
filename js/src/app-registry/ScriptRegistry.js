import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Message from 'aui-react/lib/AUIMessage';
import Icon from 'aui-react/lib/AUIIcon';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Button, {ButtonGroup} from '@atlaskit/button';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';

import {ScriptDialog} from './ScriptDialog';
import {ScriptDirectoryDialog} from './ScriptDirectoryDialog';
import {RegistryActionCreators} from './registry.reducer';

import {Script} from '../common/Script';

import {registryService} from '../service/services';

import {TitleMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import './ScriptRegistry.less';


@connect(
    state => {
        return {
            directories: state.directories
        };
    },
    RegistryActionCreators
)
export class ScriptRegistry extends React.Component {
    _setRef = (key) => (el) => {
        this[key] = el;
    };

    _activateCreateDialog = (parentId, type) => () => {
        if (type === 'directory') {
            this.directoryDialogRef.getWrappedInstance().activateCreate(parentId);
        } else {
            this.scriptDialogRef.getWrappedInstance().activateCreate(parentId);
        }
    };

    _activateEditDialog = (id, type) => () => {
        if (type === 'directory') {
            this.directoryDialogRef.getWrappedInstance().activateEdit(id);
        } else {
            this.scriptDialogRef.getWrappedInstance().activateEdit(id);
        }
    };

    _activateDeleteDialog = (id, type, name) => () => {
        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${name}"?`)) {
            if (type === 'directory') {
                registryService.deleteDirectory(id).then(() => this.props.deleteDirectory(id));
            } else {
                registryService.deleteScript(id).then(() => this.props.deleteScript(id));
            }
        }
    };

    render() {
        return (
            <Page>
                <PageHeader actions={
                    <Button
                        appearance="primary"
                        onClick={this._activateCreateDialog(null, 'directory')}
                    >
                        {RegistryMessages.addDirectory}
                    </Button>
                }>
                    {TitleMessages.registry}
                </PageHeader>

                <div className="page-content ScriptList">
                    {this.props.directories.map(directory =>
                        <ScriptDirectory
                            directory={directory}
                            key={directory.id}
                            onCreate={this._activateCreateDialog}
                            onEdit={this._activateEditDialog}
                            onDelete={this._activateDeleteDialog}
                        />
                    )}

                    {!this.props.directories.length ? <Message type="info" title={RegistryMessages.noScripts}>{RegistryMessages.noScripts}</Message> : null}
                    <ScriptDirectoryDialog ref={this._setRef('directoryDialogRef')}/>
                    <ScriptDialog ref={this._setRef('scriptDialogRef')}/>
                </div>
            </Page>
        );
    }
}

class ScriptDirectory extends React.Component {
    static propTypes = {
        directory: PropTypes.object.isRequired,
        onCreate: PropTypes.func.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired
    };

    state = {
        collapsed: false
    };

    _toggle = () => this.setState({collapsed: !this.state.collapsed});

    render() {
        const directory = this.props.directory;

        let children = null;

        if (!this.state.collapsed) {
            children = (
                <div className="scriptDirectoryChildren">
                    <div>
                        {directory.children ? directory.children.map(child =>
                            <ScriptDirectory
                                directory={child}
                                key={child.id}
                                onCreate={this.props.onCreate}
                                onEdit={this.props.onEdit}
                                onDelete={this.props.onDelete}
                            />
                        ) : null}
                    </div>
                    <div className="ScriptList">
                        {directory.scripts ? directory.scripts.map(script =>
                            <Script
                                key={script.id}
                                script={script}

                                withChangelog={true}

                                onEdit={this.props.onEdit(script.id, 'script')}
                                onDelete={this.props.onDelete(script.id, 'script', script.name)}
                            />
                        ) : null}
                    </div>
                </div>
            );
        }

        return (
            <div className="flex full-width flex-column scriptDirectory">
                <div className="scriptDirectoryTitle">
                    <div
                        onClick={this._toggle}
                        className="aui-button aui-button-link"
                    >
                        <h3>
                            <Icon icon={this.state.collapsed ? 'devtools-folder-closed' : 'devtools-folder-open'}/>
                            {' '}
                            {directory.name}
                        </h3>
                    </div>
                    <div className="pull-right">
                        <ButtonGroup>
                            <Button
                                onClick={this.props.onCreate(directory.id, 'directory')}
                            >
                                {RegistryMessages.addDirectory}
                            </Button>
                            <Button
                                onClick={this.props.onCreate(directory.id, 'script')}
                            >
                                {RegistryMessages.addScript}
                            </Button>
                            <Button
                                appearance="subtle"
                                iconBefore={<EditFilledIcon label=""/>}

                                onClick={this.props.onEdit(directory.id, 'directory')}
                            />
                            <Button
                                appearance="subtle"
                                iconBefore={<TrashIcon label=""/>}

                                onClick={this.props.onDelete(directory.id, 'directory', directory.name)}
                            />
                        </ButtonGroup>
                    </div>
                </div>
                {children}
            </div>
        );
    }
}
