//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';

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

import {RegistryActionCreators} from './registry.reducer';

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

type ScriptDirectoryConnectProps = {
    directoryWatches: Array<number>
};

type ScriptDirectoryProps = ScriptDirectoryConnectProps & {
    directory: RegistryDirectoryType,
    onCreate: CreateCallback,
    onEdit: EditCallback,
    onDelete: DeleteCallback,
    forceOpen: boolean,
    addWatch: typeof RegistryActionCreators.addWatch,
    removeWatch: typeof RegistryActionCreators.removeWatch
};

type ScriptDirectoryState = {
    collapsed: boolean,
    waitingWatch: boolean
};

class ScriptDirectoryInternal extends React.PureComponent<ScriptDirectoryProps, ScriptDirectoryState> {
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
            (error: *) => {
                this.setState({ waitingWatch: false });
                throw error;
            }
        );
    };

    _onEdit = () => this.props.onEdit(this.props.directory.id, 'directory');
    _onDelete = () => this.props.onDelete(this.props.directory.id, 'directory', this.props.directory.name);
    _onCreateDir = () => this.props.onCreate(this.props.directory.id, 'directory');
    _onCreateScript = () => this.props.onCreate(this.props.directory.id, 'script');

    render(): Node {
        const {collapsed, waitingWatch} = this.state;
        const {forceOpen, directory, directoryWatches, onCreate, onEdit, onDelete} = this.props;

        let directories: * = null;
        let scripts: * = null;

        const isOpen = !collapsed || forceOpen;

        if (isOpen) {
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
        const isWatching = directoryWatches.includes(directory.id);

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
    memoizeOne((state: *): * => {
        return {
            directoryWatches: state.directoryWatches
        };
    }),
    {
        addWatch: RegistryActionCreators.addWatch,
        removeWatch: RegistryActionCreators.removeWatch
    }
)(ScriptDirectoryInternal);
