//@flow
import React from 'react';

import {connect} from 'react-redux';
import {createSelector} from 'reselect';

import {Link} from 'react-router-dom';

import {Droppable} from 'react-beautiful-dnd';

import Button, {ButtonGroup, defaultProps as defaultButtonProps} from '@atlaskit/button';
import DropdownMenu, {DropdownItemGroup, DropdownItem} from '@atlaskit/dropdown-menu';
import Badge from '@atlaskit/badge';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import AddIcon from '@atlaskit/icon/glyph/add';
import MoreVerticalIcon from '@atlaskit/icon/glyph/more-vertical';
import FolderIcon from '@atlaskit/icon/glyph/folder';
import FolderFilledIcon from '@atlaskit/icon/glyph/folder-filled';
import WatchIcon from '@atlaskit/icon/glyph/watch';
import WatchFilledIcon from '@atlaskit/icon/glyph/watch-filled';

import {DirectoryStateActionCreators, addWatch, removeWatch} from './redux/actions';
import {groupedDirsSelector, groupedScriptsSelector} from './redux/selectors';

import {DraggableRegistryScript} from './DraggableRegistryScript';

import type {DeleteCallback, CreateCallback, EditCallback, RegistryDirectoryType, RegistryScriptType} from './types';

import {watcherService} from '../service/services';

import {CommonMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';


type ScriptDirectoryProps = {
    directory: RegistryDirectoryType,
    children: $ReadOnlyArray<RegistryDirectoryType>,
    scripts: $ReadOnlyArray<RegistryScriptType>,
    errorCount: number,
    onCreate: CreateCallback,
    onEdit: EditCallback,
    onDelete: DeleteCallback,
    open: typeof DirectoryStateActionCreators.open,
    close: typeof DirectoryStateActionCreators.close,
    forceOpen: boolean,
    isOpen: boolean,
};

export class ScriptDirectoryInternal extends React.PureComponent<ScriptDirectoryProps> {
    _toggle = () => {
        const {directory, isOpen, open, close} = this.props;

        if (isOpen) {
            close(directory.id);
        } else {
            open(directory.id);
        }
    };

    render() {
        const {forceOpen, isOpen, directory, children, scripts, errorCount, onCreate, onEdit, onDelete} = this.props;

        let directories: * = null;
        let scriptsEl: * = null;

        const hasChildren = (children.length + scripts.length) > 0;
        const open = (isOpen || forceOpen) && hasChildren;

        if (open) {
            directories = (
                <div>
                    {children && children.map(child =>
                        <ScriptDirectory
                            directory={child}
                            key={child.id}
                            forceOpen={forceOpen}
                            onCreate={onCreate}
                            onEdit={onEdit}
                            onDelete={onDelete}
                        />
                    )}
                </div>
            );
            scriptsEl = (
                scripts && scripts.map(script =>
                    <DraggableRegistryScript
                        key={script.id}
                        script={script}

                        onDelete={onDelete}
                    />
                )
            );
        }

        return (
            <div className="flex full-width flex-column scriptDirectory">
                <div className="scriptDirectoryTitle">
                    <div className="flex-row">
                        <Button
                            appearance="subtle"
                            spacing="none"
                            iconBefore={!open ? <FolderFilledIcon label=""/> : <FolderIcon label=""/>}

                            isDisabled={!hasChildren}

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
                    <div className="flex-grow"/>
                    <div className="flex-none">
                        <ScriptDirectoryActions
                            id={directory.id}
                            name={directory.name}

                            onCreate={onCreate}
                            onEdit={onEdit}
                            onDelete={onDelete}
                        />
                    </div>
                </div>
                <div className={`scriptDirectoryChildren ${open ? 'open' : ''}`}>
                    <Droppable droppableId={`${directory.id}`}>
                        {(provided, snapshot) => (
                            <div
                                ref={provided.innerRef}
                                className={`ScriptList scriptDropArea ${snapshot.isDraggingOver ? 'draggingOver' : ''}`}
                                style={{minHeight: (scriptsEl && scriptsEl.length) ? (65*scriptsEl.length + 10*(scriptsEl.length-1)) : null}}
                                {...provided.droppableProps}
                            >
                                {scriptsEl}
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

const countErrors = (dirId: number, scripts: {[?number]: ?$ReadOnlyArray<RegistryScriptType>}, dirs: {[?number]: ?$ReadOnlyArray<RegistryDirectoryType>}): number => {
    let errors: * = 0;
    if (scripts[dirId]) {
        errors += scripts[dirId].map(script => script.errorCount || 0).reduce((a, b) => a + b, 0);
    }
    if (dirs[dirId]) {
        errors += dirs[dirId].map(child => countErrors(child.id, scripts, dirs)).reduce((a, b) => a + b, 0);
    }
    return errors;
};

export const ScriptDirectory = connect(
    (): * => {
        const idSelector = (_state, props) => props.directory.id;
        const entitySelector = (entities, id) => [...(entities[id] || [])].sort((a, b) => a.name.localeCompare(b.name, undefined, {sensitivity: 'base'}));

        const childrenSelector = createSelector(
            [groupedDirsSelector, idSelector], entitySelector
        );

        const scriptsSelector = createSelector(
            [groupedScriptsSelector, idSelector], entitySelector
        );

        const isOpenSelector = createSelector(
            [state => state.openDirectories, idSelector],
            (openDirectories, id) => openDirectories.includes(id)
        );

        const errorsSelector = createSelector(
            [groupedDirsSelector, groupedScriptsSelector, idSelector],
            (dirs, scripts, id) => countErrors(id, scripts, dirs)
        );

        //$FlowFixMe
        return (state, props): * => ({
            isOpen: isOpenSelector(state, props),
            errorCount: errorsSelector(state, props),
            children: childrenSelector(state, props),
            scripts: scriptsSelector(state, props)
        });
    },
    {
        open: DirectoryStateActionCreators.open,
        close: DirectoryStateActionCreators.close
    }
)(ScriptDirectoryInternal);

type ActionsProps = {
    id: number,
    name: string,
    onCreate: CreateCallback,
    onEdit: EditCallback,
    onDelete: DeleteCallback,
    addWatch: typeof addWatch,
    removeWatch: typeof removeWatch,
    isWatched: boolean
};

type ActionsState = {
    waitingWatch: boolean
};

export class ScriptDirectoryActionsInternal extends React.PureComponent<ActionsProps, ActionsState> {
    _onEdit = () => this.props.onEdit(this.props.id, 'directory');
    _onDelete = () => this.props.onDelete(this.props.id, 'directory', this.props.name);
    _onCreateDir = () => this.props.onCreate(this.props.id, 'directory');

    _toggleWatch = () => {
        const {id, isWatched, addWatch, removeWatch} = this.props;

        this.setState({ waitingWatch: true });

        const promise = isWatched ?
            watcherService.stopWatching('REGISTRY_DIRECTORY', id) :
            watcherService.startWatching('REGISTRY_DIRECTORY', id);

        promise.then(
            () => {
                (isWatched ? removeWatch : addWatch)('directory', id);
                this.setState({ waitingWatch: false });
            },
            (error: *) => {
                this.setState({ waitingWatch: false });
                throw error;
            }
        );
    };

    state = {
        waitingWatch: false
    };

    render() {
        const {id, isWatched} = this.props;
        const {waitingWatch} = this.state;

        return (
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

                    component={Link}
                    to={`/script/create/${id}`}
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
                    iconBefore={isWatched ? <WatchFilledIcon label=""/> : <WatchIcon label=""/>}

                    onClick={this._toggleWatch}
                />
                <DropdownMenu
                    key="etc"

                    position="bottom right"

                    triggerType="button"
                    triggerButtonProps={{
                        ...defaultButtonProps,
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
        );
    }
}

const ScriptDirectoryActions = connect(
    (): * => {
        const isWatchedSelector = createSelector(
            [state => state.directoryWatches, (_state, props) => props.id],
            (directoryWatches, id) => directoryWatches.includes(id)
        );

        //$FlowFixMe
        return (state, props): * =>
            ({
                isWatched: isWatchedSelector(state, props)
            });
    },
    { addWatch, removeWatch }
)(ScriptDirectoryActionsInternal);
