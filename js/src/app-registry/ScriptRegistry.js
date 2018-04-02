import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import memoize from 'fast-memoize';
import memoizeOne from 'memoize-one';

import Message from 'aui-react/lib/AUIMessage';

import {DragDropContext, Draggable, Droppable} from 'react-beautiful-dnd';

import Page from '@atlaskit/page';
import Blanket from '@atlaskit/blanket';
import PageHeader from '@atlaskit/page-header';
import Button, {ButtonGroup} from '@atlaskit/button';
import DropdownMenu, {DropdownItemGroup, DropdownItem} from '@atlaskit/dropdown-menu';
import {FieldTextStateless} from '@atlaskit/field-text';
import Lozenge from '@atlaskit/lozenge';
import Badge from '@atlaskit/badge';
import Spinner from '@atlaskit/spinner';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';
import AddIcon from '@atlaskit/icon/glyph/add';
import MoreVerticalIcon from '@atlaskit/icon/glyph/more-vertical';
import CodeIcon from '@atlaskit/icon/glyph/code';
import FolderIcon from '@atlaskit/icon/glyph/folder';
import FolderFilledIcon from '@atlaskit/icon/glyph/folder-filled';
import ChevronRightIcon from '@atlaskit/icon/glyph/chevron-right';
import WatchIcon from '@atlaskit/icon/glyph/watch';
import WatchFilledIcon from '@atlaskit/icon/glyph/watch-filled';

import {ScriptDialog} from './ScriptDialog';
import {WorkflowsDialog} from './WorkflowsDialog';
import {ScriptDirectoryDialog} from './ScriptDirectoryDialog';
import {RegistryActionCreators} from './registry.reducer';

import {Script} from '../common/Script';

import {registryService, watcherService} from '../service/services';

import {TitleMessages} from '../i18n/common.i18n';
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

//todo: anchor to parent
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
        let {directories, ready} = this.props;

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

                        {!ready && <div className="flex-horizontal-middle"><Spinner/></div>}
                        {ready && !directories.length ? <Message type="info" title={RegistryMessages.noScripts}>{RegistryMessages.noScripts}</Message> : null}
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
        parents: PropTypes.array.isRequired,
        forceOpen: PropTypes.bool.isRequired,
        addWatch: PropTypes.func.isRequired,
        removeWatch: PropTypes.func.isRequired
    };

    static defaultProps = {
        parents: []
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

    render() {
        const {collapsed, waitingWatch} = this.state;
        const {directory, directoryWatches, parents, onCreate, onEdit, onDelete, forceOpen} = this.props;

        let directories = null;
        let scripts = null;

        if (!collapsed || forceOpen) {
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

        const errorCount = countErrors(directory);
        const isWatching = directoryWatches.includes(directory.id);

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
                        {errorCount > 0 &&
                            <div className="flex-vertical-middle" style={{marginLeft: '5px'}}>
                                <div>
                                    <Badge max={99} value={errorCount} appearance="important"/>
                                </div>
                            </div>
                        }
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
                                    <DropdownItem onClick={onDelete(directory.id, 'directory', directory.name)}>
                                        Delete
                                    </DropdownItem>
                                </DropdownItemGroup>
                            </DropdownMenu>
                        </ButtonGroup>
                    </div>
                </div>
                <div className={`scriptDirectoryChildren ${(!collapsed || forceOpen) ? 'open' : ''}`}>
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
                                                {script.name}
                                            </h3>
                                        </div>
                                        {script.errorCount > 0 &&
                                            <div className="flex-vertical-middle" style={{marginLeft: '5px'}}>
                                                <div>
                                                    <Badge max={99} value={script.errorCount} appearance="important"/>
                                                </div>
                                            </div>
                                        }
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
        removeWatch: PropTypes.func.isRequired
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

    render() {
        const {script, onEdit, onDelete, scriptWatches, ...props} = this.props;
        const {showWorkflows, waitingWatch} = this.state;

        const isWatching = scriptWatches.includes(script.id);

        return (
            <div>
                {showWorkflows && <WorkflowsDialog id={script.id} onClose={this._toggleWorkflows}/>}
                <Script
                    script={script}

                    withChangelog={true}

                    onEdit={onEdit}
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
                                    Find workflows
                                </DropdownItem>
                                <DropdownItem onClick={onDelete}>
                                    Delete
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
