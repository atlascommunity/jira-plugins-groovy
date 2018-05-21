//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';
import {Link} from 'react-router-dom';

import memoize from 'fast-memoize';
import memoizeOne from 'memoize-one';

import {Droppable} from 'react-beautiful-dnd';

import Button, {ButtonGroup} from '@atlaskit/button';
import DropdownMenu, {DropdownItemGroup, DropdownItem} from '@atlaskit/dropdown-menu';
import Badge from '@atlaskit/badge';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import AddIcon from '@atlaskit/icon/glyph/add';
import MoreVerticalIcon from '@atlaskit/icon/glyph/more-vertical';
import FolderIcon from '@atlaskit/icon/glyph/folder';
import FolderFilledIcon from '@atlaskit/icon/glyph/folder-filled';
import WatchIcon from '@atlaskit/icon/glyph/watch';
import WatchFilledIcon from '@atlaskit/icon/glyph/watch-filled';

import {DirectoryStateActionCreators, RegistryActionCreators} from './registry.reducer';

import {DraggableRegistryScript} from './DraggableRegistryScript';

import type {DeleteCallback, CreateCallback, EditCallback, RegistryDirectoryType} from './types';

import {watcherService} from '../service/services';

import {CommonMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';


const countErrors = memoize((directory: RegistryDirectoryType): number => {
    let errors: * = 0;
    if (directory.scripts) {
        errors += directory.scripts.map(script => script.errorCount || 0).reduce((a, b) => a + b, 0);
    }
    if (directory.children) {
        errors += directory.children.map(child => countErrors(child)).reduce((a, b) => a + b, 0);
    }
    return errors;
});

type ScriptDirectoryProps = {
    directory: RegistryDirectoryType,
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

    render(): Node {
        const {forceOpen, isOpen, directory, onCreate, onEdit, onDelete} = this.props;

        let directories: * = null;
        let scripts: * = null;

        if (isOpen || forceOpen) {
            directories = (
                <div>
                    {directory.children ? directory.children.map(child =>
                        <ScriptDirectory
                            directory={child}
                            key={child.id}
                            forceOpen={forceOpen}
                            onCreate={onCreate}
                            onEdit={onEdit}
                            onDelete={onDelete}
                        />
                    ) : null}
                </div>
            );
            scripts = (
                directory.scripts && directory.scripts.map(script =>
                    <DraggableRegistryScript
                        key={script.id}
                        script={script}

                        onEdit={onEdit}
                        onDelete={onDelete}
                    />
                )
            );
        }

        const errorCount = countErrors(directory);

        return (
            <div className="flex full-width flex-column scriptDirectory">
                <div className="scriptDirectoryTitle">
                    <div className="flex-row">
                        <Button
                            appearance="subtle"
                            spacing="none"
                            iconBefore={!isOpen ? <FolderFilledIcon label=""/> : <FolderIcon label=""/>}

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
                <div className={`scriptDirectoryChildren ${isOpen ? 'open' : ''}`}>
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

export const ScriptDirectory = connect(
    () => memoizeOne(
        //$FlowFixMe
        ({openDirectories}: *, {directory}: *): * => {
            return {
                isOpen: openDirectories.includes(directory.id)
            };
        }
    ),
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
    addWatch: typeof RegistryActionCreators.addWatch,
    removeWatch: typeof RegistryActionCreators.removeWatch,
    isOpen: boolean,
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

    render(): Node {
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
        //$FlowFixMe
        return ({directoryWatches}, {id}): * =>
            ({
                isWatched: directoryWatches.includes(id)
            });
    },
    {
        addWatch: RegistryActionCreators.addWatch,
        removeWatch: RegistryActionCreators.removeWatch
    }
)(ScriptDirectoryActionsInternal);
