import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import memoize from 'fast-memoize';
import memoizeOne from 'memoize-one';

import {DragDropContext, Draggable, Droppable} from 'react-beautiful-dnd';

import Page from '@atlaskit/page';
import Blanket from '@atlaskit/blanket';
import PageHeader from '@atlaskit/page-header';
import Button, {ButtonGroup} from '@atlaskit/button';
import DropdownMenu, {DropdownItemGroup, DropdownItem} from '@atlaskit/dropdown-menu';
import {FieldTextStateless} from '@atlaskit/field-text';
import Lozenge from '@atlaskit/lozenge';
import Badge from '@atlaskit/badge';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import AddIcon from '@atlaskit/icon/glyph/add';
import MoreVerticalIcon from '@atlaskit/icon/glyph/more-vertical';
import CodeIcon from '@atlaskit/icon/glyph/code';
import FolderIcon from '@atlaskit/icon/glyph/folder';
import FolderFilledIcon from '@atlaskit/icon/glyph/folder-filled';
import WatchIcon from '@atlaskit/icon/glyph/watch';
import WatchFilledIcon from '@atlaskit/icon/glyph/watch-filled';

import {ScriptDialog} from './ScriptDialog';
import {WorkflowsDialog} from './WorkflowsDialog';
import {ScriptDirectoryDialog} from './ScriptDirectoryDialog';
import {RegistryActionCreators} from './registry.reducer';

import Script from '../common/script';
import {LoadingSpinner} from '../common/ak/LoadingSpinner';
import {InfoMessage} from '../common/ak/messages';

import {registryService, watcherService} from '../service/services';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import './ScriptRegistry.less';


const countErrors = memoize((directory) => {
    let errors = 0;
    if (directory.scripts) {
        errors += directory.scripts.map(script => script.errorCount || 0).reduce((a, b) => a + b, 0);
    }
    if (directory.children) {
        errors += directory.children.map(child => countErrors(child)).reduce((a, b) => a + b, 0);
    }
    return errors;
});

//todo: collapse/uncollapse all
@connect(
    memoizeOne(
        state => {
            return {
                ready: state.ready,
                directories: state.directories
            };
        }
    ),
    RegistryActionCreators
)
export class ScriptRegistry extends React.Component {
    static propTypes = {
        ready: PropTypes.bool.isRequired,
        directories: PropTypes.arrayOf(PropTypes.object.isRequired)
    };

    state = {
        isDragging: false,
        waiting: false,
        filter: ''
    };

    _setRef = (key) => (el) => {
        this[key] = el;
    };

    _activateCreateDialog = (parentId, type) => {
        if (type === 'directory') {
            this.directoryDialogRef.getWrappedInstance().activateCreate(parentId);
        } else {
            this.scriptDialogRef.getWrappedInstance().activateCreate(parentId);
        }
    };

    _activateEditDialog = (id, type) => {
        console.log(id, type);
        if (type === 'directory') {
            this.directoryDialogRef.getWrappedInstance().activateEdit(id);
        } else {
            this.scriptDialogRef.getWrappedInstance().activateEdit(id);
        }
    };

    _activateDeleteDialog = (id, type, name) => {
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

    _matchesFilter = (item, filter) => (item.name.toLocaleLowerCase().includes(filter));

    _getFilteredDirsInternal = (dirs, filter) => {
        return dirs
            .map(dir => {
                if (this._matchesFilter(dir, filter)) {
                    return dir;
                }
                return {
                    ...dir,
                    scripts: dir.scripts.filter(script => this._matchesFilter(script, filter)),
                    children: this._getFilteredDirsInternal(dir.children)
                };
            })
            .filter(dir => (dir.scripts.length + dir.children.length) > 0);
    };

    _getFilteredDirs = memoizeOne(this._getFilteredDirsInternal);

    render() {
        const {waiting, filter} = this.state;
        let {directories, ready} = this.props;

        if (filter && filter.length >= 2) {
            directories = this._getFilteredDirs(directories, filter.toLocaleLowerCase());
        }

        return (
            <DragDropContext onDragStart={this._onDragStart} onDragEnd={this._onDragEnd}>
                <Page>
                    <PageHeader
                        actions={
                            <Button
                                appearance="primary"
                                onClick={this._createDirectory}
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
                            />
                        )}

                        {!ready && <LoadingSpinner/>}
                        {ready && !directories.length ? <InfoMessage title={RegistryMessages.noScripts}/> : null}
                        <ScriptDirectoryDialog ref={this._setRef('directoryDialogRef')}/>
                        <ScriptDialog ref={this._setRef('scriptDialogRef')}/>

                        {waiting && <Blanket isTinted={true}/>}
                    </div>
                </Page>
            </DragDropContext>
        );
    }
}

@connect(
    memoizeOne(state => {
        return {
            directoryWatches: state.directoryWatches
        };
    }),
    RegistryActionCreators
)
class ScriptDirectory extends React.Component {
    static propTypes = {
        directory: PropTypes.object.isRequired,
        directoryWatches: PropTypes.arrayOf(PropTypes.number.isRequired).isRequired,
        onCreate: PropTypes.func.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired,
        addWatch: PropTypes.func.isRequired,
        removeWatch: PropTypes.func.isRequired
    };

    state = {
        collapsed: true,
        waitingWatch: false
    };

    _toggle = () => this.setState({collapsed: !this.state.collapsed});

    _toggleWatch = () => {
        const {directory, directoryWatches, addWatch, removeWatch} = this.props;

        const isWatching = directoryWatches.includes(directory.id);

        this.setState({ waitingWatch: true });

        const promise = isWatching ?
            watcherService.stopWatching('REGISTRY_DIRECTORY', directory.id) :
            watcherService.startWatching('REGISTRY_DIRECTORY', directory.id);

        promise.then(
            () => {
                (isWatching ? removeWatch : addWatch)('directory', directory.id);
                this.setState({ waitingWatch: false });
            },
            error => {
                this.setState({ waitingWatch: false });
                throw error;
            }
        );
    };

    _onEdit = () => this.props.onEdit(this.props.directory.id, 'directory');
    _onDelete = () => this.props.onDelete(this.props.directory.id, 'directory', this.props.directory.name);
    _onCreateDir = () => this.props.onCreate(this.props.directory.id, 'directory');
    _onCreateScript = () => this.props.onCreate(this.props.directory.id, 'script');

    render() {
        const {collapsed, waitingWatch} = this.state;
        const {directory, directoryWatches, onCreate, onEdit, onDelete} = this.props;

        let directories = null;
        let scripts = null;

        if (!collapsed) {
            directories = (
                <div>
                    {directory.children ? directory.children.map(child =>
                        <ScriptDirectory
                            directory={child}
                            directoryWatches={directoryWatches}
                            key={child.id}
                            onCreate={onCreate}
                            onEdit={onEdit}
                            onDelete={onDelete}

                            addWatch={this.props.addWatch}
                            removeWatch={this.props.removeWatch}
                        />
                    ) : null}
                </div>
            );
            scripts = (
                directory.scripts && directory.scripts.map(script =>
                    <DraggableScript
                        key={script.id}
                        script={script}

                        onEdit={onEdit}
                        onDelete={onDelete}
                    />
                )
            );
        }

        const errorCount = countErrors(directory);
        const isWatching = directoryWatches.includes(directory.id);

        return (
            <div className="flex full-width flex-column scriptDirectory">
                <div className="scriptDirectoryTitle">
                    <div className="flex-grow flex-row">
                        <Button
                            appearance="subtle"
                            spacing="none"
                            iconBefore={collapsed ? <FolderFilledIcon label=""/> : <FolderIcon label=""/>}

                            isDisabled={(directory.children.length + directory.scripts.length) === 0}

                            onClick={this._toggle}
                        >
                            <h3 className="flex-vertical-middle" style={{ margin: 0 }}>
                                {' '}{directory.name}
                            </h3>
                        </Button>
                    </div>
                    {errorCount > 0 &&
                        <div className="flex-vertical-middle flex-none errorCount">
                            <div>
                                <Badge max={99} value={errorCount} appearance="important"/>
                            </div>
                        </div>
                    }
                    <div className="flex-none">
                        <ButtonGroup>
                            <Button
                                appearance="subtle"
                                iconBefore={<AddIcon label=""/>}

                                onClick={this._onCreateDir}
                            >
                                {RegistryMessages.addDirectory}
                            </Button>
                            <Button
                                appearance="subtle"
                                iconBefore={<AddIcon label=""/>}

                                onClick={this._onCreateScript}
                            >
                                {RegistryMessages.addScript}
                            </Button>
                            <Button
                                appearance="subtle"
                                iconBefore={<EditFilledIcon label=""/>}

                                onClick={this._onEdit}
                            />
                            <Button
                                key="watch"
                                appearance="subtle"
                                isDisabled={waitingWatch}
                                iconBefore={isWatching ? <WatchFilledIcon label=""/> : <WatchIcon label=""/>}

                                onClick={this._toggleWatch}
                            />
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
                                    <DropdownItem onClick={this._onDelete}>
                                        {CommonMessages.delete}
                                    </DropdownItem>
                                </DropdownItemGroup>
                            </DropdownMenu>
                        </ButtonGroup>
                    </div>
                </div>
                <div className={`scriptDirectoryChildren ${!collapsed ? 'open' : ''}`}>
                    <Droppable droppableId={`${directory.id}`}>
                        {(provided, snapshot) => (
                            <div
                                ref={provided.innerRef}
                                className={`ScriptList scriptDropArea ${snapshot.isDraggingOver ? 'draggingOver' : ''}`}
                                style={{minHeight: (scripts && scripts.length) ? (65*scripts.length + 10*(scripts.length-1)) : null}}
                                {...provided.droppableProps}
                            >
                                {scripts}
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
        const {script} = this.props;

        return (
            <div className="DraggableScript">
                <Draggable draggableId={`${this.props.script.id}`} type="script">
                    {(provided) => (
                        <RegistryScript
                            title={
                                <div className="flex-grow flex-row" {...provided.dragHandleProps}>
                                    <div className="flex-vertical-middle flex-none">
                                        <CodeIcon label=""/>
                                    </div>
                                    {' '}
                                    <div className="flex-vertical-middle">
                                        <h3 title={script.name}>
                                            {script.name}
                                        </h3>
                                    </div>
                                    {script.errorCount > 0 &&
                                        <div className="flex-vertical-middle flex-none errorCount">
                                            <div>
                                                <Badge max={99} value={script.errorCount} appearance="important"/>
                                            </div>
                                        </div>
                                    }
                                </div>
                            }
                            wrapperProps={{ ...provided.draggableProps, ref: provided.innerRef }}
                            {...this.props}
                        />
                    )}
                </Draggable>
            </div>
        );
    }
}

@connect(
    memoizeOne(state => {
        return {
            scriptWatches: state.scriptWatches
        };
    }),
    RegistryActionCreators
)
class RegistryScript extends React.Component {
    static propTypes = {
        script: PropTypes.object.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired,
        scriptWatches: PropTypes.arrayOf(PropTypes.number.isRequired).isRequired,
        addWatch: PropTypes.func.isRequired,
        removeWatch: PropTypes.func.isRequired,
        wrapperProps: PropTypes.any
    };

    state = {
        showWorkflows: false,
        waitingWatch: false
    };

    _toggleWorkflows = () => {
        this.setState(state => {
            return {
                showWorkflows: !state.showWorkflows
            };
        });
    };

    _toggleWatch = () => {
        const {script, scriptWatches, addWatch, removeWatch} = this.props;

        const isWatching = scriptWatches.includes(script.id);

        this.setState({ waitingWatch: true });

        const promise = isWatching ?
            watcherService.stopWatching('REGISTRY_SCRIPT', script.id) :
            watcherService.startWatching('REGISTRY_SCRIPT', script.id);

        promise.then(
            () => {
                (isWatching ? removeWatch : addWatch)('script', script.id);
                this.setState({ waitingWatch: false });
            },
            error => {
                this.setState({ waitingWatch: false });
                throw error;
            }
        );
    };

    _onEdit = () => this.props.onEdit(this.props.script.id, 'script');
    _onDelete = () => this.props.onDelete(this.props.script.id, 'script', this.props.script.name);

    render() {
        const {script, scriptWatches, wrapperProps, ...props} = this.props;
        const {showWorkflows, waitingWatch} = this.state;

        const isWatching = scriptWatches.includes(script.id);

        return (
            <div {...wrapperProps}>
                {showWorkflows && <WorkflowsDialog id={script.id} onClose={this._toggleWorkflows}/>}
                <Script
                    {...props}

                    script={script}

                    withChangelog={true}

                    onEdit={this._onEdit}
                    additionalButtons={[
                        <Button
                            key="watch"
                            appearance="subtle"
                            isDisabled={waitingWatch}
                            iconBefore={isWatching ? <WatchFilledIcon label=""/> : <WatchIcon label=""/>}

                            onClick={this._toggleWatch}
                        />,
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
                                    {RegistryMessages.findWorkflows}
                                </DropdownItem>
                                <DropdownItem onClick={this._onDelete}>
                                    {CommonMessages.delete}
                                </DropdownItem>
                            </DropdownItemGroup>
                        </DropdownMenu>
                    ]}
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
