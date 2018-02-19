import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Message from 'aui-react/lib/AUIMessage';

import {DragDropContext, Draggable, Droppable} from 'react-beautiful-dnd';

import Page from '@atlaskit/page';
import Blanket from '@atlaskit/blanket';
import PageHeader from '@atlaskit/page-header';
import Button, {ButtonGroup} from '@atlaskit/button';
import DropdownMenu, {DropdownItemGroup, DropdownItem} from '@atlaskit/dropdown-menu';
import {FieldTextStateless} from '@atlaskit/field-text';
import Lozenge from '@atlaskit/lozenge';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';
import AddIcon from '@atlaskit/icon/glyph/add';
import MoreVerticalIcon from '@atlaskit/icon/glyph/more-vertical';
import CodeIcon from '@atlaskit/icon/glyph/code';
import FolderIcon from '@atlaskit/icon/glyph/folder';
import FolderFilledIcon from '@atlaskit/icon/glyph/folder-filled';
import ChevronRightIcon from '@atlaskit/icon/glyph/chevron-right';

import {ScriptDialog} from './ScriptDialog';
import {WorkflowsDialog} from './WorkflowsDialog';
import {ScriptDirectoryDialog} from './ScriptDirectoryDialog';
import {RegistryActionCreators} from './registry.reducer';

import {Script} from '../common/Script';

import {registryService} from '../service/services';

import {TitleMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import './ScriptRegistry.less';


//todo: разобраться почему исходный блок ужимается при перетаскивании.
//todo: anchor to parent
//todo: collapse/uncollapse all
@connect(
    state => {
        return {
            directories: state.directories
        };
    },
    RegistryActionCreators
)
export class ScriptRegistry extends React.Component {
    state = {
        isDragging: false,
        waiting: false,
        filter: ''
    };

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

    _onDragStart = () => {
        this.setState({isDragging: true});
    };

    _onDragEnd = (result) => {
        const {source, destination, draggableId} = result;

        this.setState({isDragging: false});

        if (!source || !destination) {
            return;
        }

        if (source.droppableId === destination.droppableId) {
            return;
        }

        const sourceId = parseInt(source.droppableId, 10);
        const destId = parseInt(destination.droppableId, 10);
        const scriptId = parseInt(draggableId, 10);

        const script = this._findScript(scriptId);

        if (!script) {
            console.error('unable to find script', result);
            return;
        }

        this.props.moveScript(sourceId, destId, script);

        this.setState({ waiting: true });

        registryService
            .moveScript(script.id, destId)
            .then(
                () => {
                    this.setState({ waiting: false });
                    //todo: maybe show flag that script was successfully moved
                },
                () => {
                    this.props.moveScript(destId, sourceId, script); //move script to old parent
                    this.setState({ waiting: false });
                }
            );
    };

    _findScript(id) {
        for (const dir of this.props.directories) {
            const found = this._findScriptInDirectory(dir, id);
            if (found) {
                return found;
            }
        }
        return null;
    }

    _findScriptInDirectory(dir, id) {
        if (dir.children) {
            for (const child of dir.children) {
                const foundInDir = this._findScriptInDirectory(child, id);
                if (foundInDir) {
                    return foundInDir;
                }
            }
        }
        if (dir.scripts) {
            return dir.scripts.find(script => script.id === id);
        }
        return null;
    }

    _onFilterChange = (e) => this.setState({ filter: e.target.value });

    _matchesFilter = (item) => (item.name.toLocaleLowerCase().includes(this.state.filter.toLocaleLowerCase()));

    _getFilteredDirs = (dirs) => {
        return dirs
            .map(dir => {
                if (this._matchesFilter(dir)) {
                    return dir;
                }
                return {
                    ...dir,
                    scripts: dir.scripts.filter(this._matchesFilter),
                    children: this._getFilteredDirs(dir.children)
                };
            })
            .filter(dir => (dir.scripts.length + dir.children.length) > 0);
    };

    render() {
        const {waiting, filter} = this.state;

        let directories = this.props.directories;
        let forceOpen = false;

        if (filter && filter.length > 0) {
            directories = this._getFilteredDirs(directories);
            forceOpen = true;
        }

        return (
            <DragDropContext onDragStart={this._onDragStart} onDragEnd={this._onDragEnd}>
                <Page>
                    <PageHeader
                        actions={
                            <Button
                                appearance="primary"
                                onClick={this._activateCreateDialog(null, 'directory')}
                            >
                                {RegistryMessages.addDirectory}
                            </Button>
                        }
                        bottomBar={
                            <FieldTextStateless
                                isLabelHidden
                                compact
                                label="hidden"
                                placeholder="Filter"
                                value={filter}
                                onChange={this._onFilterChange}
                            />
                        }
                    >
                        {TitleMessages.registry}
                    </PageHeader>

                    <div className={`page-content ScriptList ${this.state.isDragging ? 'dragging' : ''}`}>
                        {directories.map(directory =>
                            <ScriptDirectory
                                directory={directory}
                                key={directory.id}
                                onCreate={this._activateCreateDialog}
                                onEdit={this._activateEditDialog}
                                onDelete={this._activateDeleteDialog}

                                forceOpen={forceOpen}
                            />
                        )}

                        {!directories.length ? <Message type="info" title={RegistryMessages.noScripts}>{RegistryMessages.noScripts}</Message> : null}
                        <ScriptDirectoryDialog ref={this._setRef('directoryDialogRef')}/>
                        <ScriptDialog ref={this._setRef('scriptDialogRef')}/>

                        {waiting && <Blanket isTinted={true}/>}
                    </div>
                </Page>
            </DragDropContext>
        );
    }
}

class ScriptDirectory extends React.Component {
    static propTypes = {
        directory: PropTypes.object.isRequired,
        onCreate: PropTypes.func.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired,
        parents: PropTypes.array.isRequired,
        forceOpen: PropTypes.bool.isRequired
    };

    static defaultProps = {
        parents: []
    };

    state = {
        collapsed: true
    };

    _toggle = () => this.setState({collapsed: !this.state.collapsed});

    render() {
        const {collapsed} = this.state;
        const {directory, parents, onCreate, onEdit, onDelete, forceOpen} = this.props;

        let directories = null;
        let scripts = null;

        if (!collapsed || forceOpen) {
            directories = (
                <div>
                    {directory.children ? directory.children.map(child =>
                        <ScriptDirectory
                            directory={child}
                            key={child.id}
                            onCreate={onCreate}
                            onEdit={onEdit}
                            onDelete={onDelete}
                            forceOpen={forceOpen}

                            parents={[directory, ...parents]}
                        />
                    ) : null}
                </div>
            );
            scripts = (
                directory.scripts && directory.scripts.map(script =>
                    <DraggableScript
                        key={script.id}
                        script={script}

                        onEdit={onEdit(script.id, 'script')}
                        onDelete={onDelete(script.id, 'script', script.name)}
                    />
                )
            );
        }

        return (
            <div className="flex full-width flex-column scriptDirectory">
                <div className="scriptDirectoryTitle">
                    <div className="flex-none flex-row">
                        <div className="flex-vertical-middle">
                            <Button
                                appearance="subtle"
                                spacing="none"
                                iconBefore={collapsed ? <FolderFilledIcon label=""/> : <FolderIcon label=""/>}

                                isDisabled={(directory.children.length + directory.scripts.length) === 0}

                                onClick={this._toggle}
                            >
                                <h3 className="flex-vertical-middle" style={{ margin: 0 }}>
                                    <span>{' '}{directory.name}</span>
                                </h3>
                            </Button>
                        </div>
                        <div className="muted-text flex-vertical-middle">
                            <div className="flex-row">
                                {parents.map((parent) =>
                                    <div key={parent.id} className="flex-row">
                                        <ChevronRightIcon label=""/>
                                        <div className="flex-vertical-middle">
                                            {parent.name}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                    <div className="flex-grow"/>
                    <div className="flex-none">
                        <ButtonGroup>
                            <Button
                                appearance="subtle"
                                iconBefore={<AddIcon label=""/>}

                                onClick={onCreate(directory.id, 'directory')}
                            >
                                {RegistryMessages.addDirectory}
                            </Button>
                            <Button
                                appearance="subtle"
                                iconBefore={<AddIcon label=""/>}

                                onClick={onCreate(directory.id, 'script')}
                            >
                                {RegistryMessages.addScript}
                            </Button>
                            <Button
                                appearance="subtle"
                                iconBefore={<EditFilledIcon label=""/>}

                                onClick={onEdit(directory.id, 'directory')}
                            />
                            <Button
                                appearance="subtle"
                                iconBefore={<TrashIcon label=""/>}

                                onClick={onDelete(directory.id, 'directory', directory.name)}
                            />
                        </ButtonGroup>
                    </div>
                </div>
                <div className="scriptDirectoryChildren">
                    <Droppable droppableId={`${directory.id}`}>
                        {(provided, snapshot) => (
                            <div
                                ref={provided.innerRef}
                                className={`ScriptList scriptDropArea ${snapshot.isDraggingOver ? 'draggingOver' : ''}`}
                            >
                                {scripts}
                                {!(scripts && scripts.length) && <div className="dropFiller"/>}
                                {provided.placeholder}
                            </div>
                        )}
                    </Droppable>
                    {directories}
                </div>
            </div>
        );
    }
}

class DraggableScript extends React.Component {
    static propTypes = {
        script: PropTypes.object.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired
    };

    render() {
        return (
            <div className="DraggableScript">
                <Draggable draggableId={`${this.props.script.id}`} type="script">
                    {(provided, snapshot) => (
                        <div
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                        >
                            <RegistryScript
                                title={
                                    <div className="flex-grow flex-row" {...provided.dragHandleProps}>
                                        <div className="flex-vertical-middle">
                                            <CodeIcon label=""/>
                                        </div>
                                        {' '}
                                        <div className="flex-vertical-middle">
                                            <h3>
                                                {this.props.script.name}
                                            </h3>
                                        </div>
                                    </div>
                                }
                                {...this.props}
                            />
                            {provided.placeholder}
                        </div>
                    )}
                </Draggable>
            </div>
        );
    }
}

class RegistryScript extends React.Component {
    static propTypes = {
        script: PropTypes.object.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired
    };

    state = {
        showWorkflows: false
    };

    _toggleWorkflows = () => {
        this.setState(state => {
            return {
                showWorkflows: !state.showWorkflows
            };
        });
    };

    render() {
        const {script, onEdit, onDelete, ...props} = this.props;
        const {showWorkflows} = this.state;

        return (

            <div>
                {showWorkflows && <WorkflowsDialog id={script.id} onClose={this._toggleWorkflows}/>}
                <Script
                    script={script}

                    withChangelog={true}

                    onEdit={onEdit}
                    onDelete={onDelete}
                    additionalButtons={[
                        <DropdownMenu
                            key="etc"

                            position="bottom right"

                            triggerType="button"
                            triggerButtonProps={{
                                appearance: 'subtle',
                                iconBefore: <MoreVerticalIcon label=""/>
                            }}
                        >
                            <DropdownItemGroup>
                                <DropdownItem onClick={this._toggleWorkflows}>
                                    Find workflows
                                </DropdownItem>
                            </DropdownItemGroup>
                        </DropdownMenu>
                    ]}

                    {...props}
                >
                    <div className="flex-row" style={{marginBottom: '5px'}}>
                        {script.types.map(type =>
                            <div style={{marginRight: '5px'}} key={type}>
                                <Lozenge appearance="new" isBold={true}>{type}</Lozenge>
                            </div>
                        )}
                    </div>
                </Script>
            </div>
        );
    }
}
