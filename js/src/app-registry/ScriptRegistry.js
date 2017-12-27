import React from 'react';

import {connect} from 'react-redux';

import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

import PropTypes from 'prop-types';

import {ScriptDialog} from './ScriptDialog';
import {ScriptDirectoryDialog} from './ScriptDirectoryDialog';
import {RegistryActionCreators} from './registry.reducer';

import {Script} from '../common/Script';

import {registryService} from '../service/services';

import {CommonMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import './ScriptRegistry.less';


//todo: check leaks
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
            <div className="full-width">
                <Button
                    type="primary"
                    icon="add"
                    onClick={this._activateCreateDialog(null, 'directory')}
                >
                    {RegistryMessages.addDirectory}
                </Button>

                {this.props.directories.map(directory =>
                    <ScriptDirectory
                        directory={directory}
                        key={directory.id}
                        onCreate={this._activateCreateDialog}
                        onEdit={this._activateEditDialog}
                        onDelete={this._activateDeleteDialog}
                    />
                )}

                {!this.props.directories.length ? <Message type="info" title="You have no scripts.">{RegistryMessages.noScripts}</Message> : null}
                <ScriptDirectoryDialog ref={this._setRef('directoryDialogRef')}/>
                <ScriptDialog ref={this._setRef('scriptDialogRef')}/>
            </div>
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
                    <div>
                        {directory.scripts ? directory.scripts.map(script =>
                            <Script
                                key={script.id}
                                script={script}

                                withChangelog={true}
                                editable={true}

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
                    <Button
                        icon={this.state.collapsed ? 'devtools-folder-closed' : 'devtools-folder-open'}
                        onClick={this._toggle}
                        type="link"
                    >
                        {directory.name}
                    </Button>
                    <div className="pull-right">
                        <Button icon="add" type="subtle" onClick={this.props.onCreate(directory.id, 'directory')}>{RegistryMessages.addDirectory}</Button>
                        <Button icon="add" type="subtle" onClick={this.props.onCreate(directory.id, 'script')}>{RegistryMessages.addScript}</Button>
                        <Button icon="edit" type="subtle" onClick={this.props.onEdit(directory.id, 'directory')}>{CommonMessages.edit}</Button>
                        <Button icon="delete" type="subtle" onClick={this.props.onDelete(directory.id, 'directory', directory.name)}>{CommonMessages.delete}</Button>
                    </div>
                </div>
                {children}
            </div>
        );
    }
}
