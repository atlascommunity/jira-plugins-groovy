//@flow
import React from 'react';

import {connect} from 'react-redux';
import {createSelector} from 'reselect';

import {DragDropContext} from 'react-beautiful-dnd';

import Page from '@atlaskit/page';
import Blanket from '@atlaskit/blanket';
import PageHeader from '@atlaskit/page-header';
import Button from '@atlaskit/button';
import {FieldTextStateless} from '@atlaskit/field-text';
import {CheckboxStateless} from '@atlaskit/checkbox';

import {ScriptDirectory} from './ScriptDirectory';
import {ScriptDirectoryDialog, type DialogParams} from './ScriptDirectoryDialog';
import {RegistryActionCreators, UpdateActionCreators as FilterActionCreators} from './redux/actions';
import {filteredSelector, filterSelector, groupedDirsSelector} from './redux/selectors';
import {UsageStatusFlag} from './UsageStatusFlag';

import type {FilterType, RegistryDirectoryType} from './types';

import {InfoMessage} from '../common/ak/messages';

import {registryService} from '../service/services';

import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import './ScriptRegistry.less';
import type {DeleteDialogProps} from '../common/script-list/DeleteDialog';
import {DeleteDialog} from '../common/script-list/DeleteDialog';


type Props = {
    directories: $ReadOnlyArray<RegistryDirectoryType>,
    isScriptUsageReady: boolean,
    isForceOpen: boolean,
    filter: FilterType,
    deleteDirectory: typeof RegistryActionCreators.deleteDirectory,
    deleteScript: typeof RegistryActionCreators.deleteScript,
    moveScript: typeof RegistryActionCreators.moveScript,
    updateFilter: typeof FilterActionCreators.updateFilter
};

type State = {
    waiting: boolean,
    isDragging: boolean,
    directoryDialogProps: ?DialogParams,
    deleteScriptProps: ?DeleteDialogProps,
    deleteDirectoryProps: ?DeleteDialogProps
};

//todo: collapse/uncollapse all
export class ScriptRegistryInternal extends React.PureComponent<Props, State> {
    state = {
        isDragging: false,
        waiting: false,
        onlyUnused: false,
        directoryDialogProps: null,
        deleteScriptProps: null,
        deleteDirectoryProps: null,
        filter: ''
    };

    _activateCreateDialog = (parentId: ?number, type: 'script'|'directory') => {
        if (type === 'directory') {
            this.setState({ directoryDialogProps: {isNew: true, parentId} });
        }
    };

    _activateEditDialog = (id: number, type: 'script'|'directory') => {
        if (type === 'directory') {
            this.setState({ directoryDialogProps: {isNew: false, id} });
        }
    };

    _closeDirectoryDialog = () => this.setState({ directoryDialogProps: null });

    _closeDeleteScriptDialog = () => this.setState({ deleteScriptProps: null });

    _closeDeleteDirectoryDialog = () => this.setState({ deleteDirectoryProps: null });

    _activateDeleteDialog = (id: number, type: 'script'|'directory', name: string) => {
        if (type === 'directory') {
            this.setState({
                deleteDirectoryProps: { id, name, onConfirm: () => registryService.deleteDirectory(id) }
            });
        } else {
            this.setState({
                deleteScriptProps: { id, name, onConfirm: () => registryService.deleteScript(id) }
            });
        }
    };

    _createDirectory = () => this._activateCreateDialog(null, 'directory');

    _onDragStart = () => {
        this.setState({isDragging: true});
    };

    _onDragEnd = (result: *) => {
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

        this.props.moveScript(sourceId, destId, scriptId);

        this.setState({ waiting: true });

        registryService
            .moveScript(scriptId, destId)
            .then(
                () => {
                    this.setState({ waiting: false });
                    //todo: maybe show flag that script was successfully moved
                },
                () => {
                    this.props.moveScript(destId, sourceId, scriptId); //move script to old parent
                    this.setState({ waiting: false });
                }
            );
    };

    _onFilterChange = (e: SyntheticEvent<HTMLInputElement>) => this.props.updateFilter({ name: e.currentTarget.value });

    _toggleUnused = () => this.props.updateFilter({ onlyUnused: !this.props.filter.onlyUnused });

    render() {
        const {waiting, directoryDialogProps, deleteScriptProps, deleteDirectoryProps} = this.state;
        const {isScriptUsageReady, isForceOpen, filter, deleteScript, deleteDirectory} = this.props;

        let directories: * = this.props.directories;

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
                            <div className="flex-row">
                                <FieldTextStateless
                                    isLabelHidden
                                    compact
                                    label="hidden"
                                    placeholder="Filter"
                                    value={filter.name}
                                    onChange={this._onFilterChange}
                                />
                                <div className="flex-vertical-middle">
                                    <CheckboxStateless
                                        label={RegistryMessages.onlyUnused}
                                        isDisabled={!isScriptUsageReady}

                                        isChecked={filter.onlyUnused}
                                        onChange={this._toggleUnused}
                                    />
                                </div>
                            </div>
                        }
                    >
                        {TitleMessages.registry}
                    </PageHeader>

                    <div className={`page-content ScriptList ${this.state.isDragging ? 'dragging' : ''}`}>
                        {directories.map(directory =>
                            <ScriptDirectory
                                directory={directory}
                                key={directory.id}

                                forceOpen={isForceOpen}

                                onCreate={this._activateCreateDialog}
                                onEdit={this._activateEditDialog}
                                onDelete={this._activateDeleteDialog}
                            />
                        )}

                        {!directories.length ? <InfoMessage title={RegistryMessages.noScripts}/> : null}
                        {directoryDialogProps && <ScriptDirectoryDialog {...directoryDialogProps} onClose={this._closeDirectoryDialog}/>}
                        {deleteDirectoryProps &&
                            <DeleteDialog
                                deleteItem={deleteDirectory}
                                onClose={this._closeDeleteDirectoryDialog}
                                i18n={{
                                    heading: RegistryMessages.deleteDirectory,
                                    areYouSure: CommonMessages.confirmDelete
                                }}

                                {...deleteDirectoryProps}
                            />
                        }
                        {deleteScriptProps &&
                            <DeleteDialog
                                deleteItem={deleteScript}
                                onClose={this._closeDeleteScriptDialog}
                                i18n={{
                                    heading: RegistryMessages.deleteScript,
                                    areYouSure: CommonMessages.confirmDelete
                                }}

                                {...deleteScriptProps}
                            />
                        }

                        {waiting && <Blanket isTinted={true}/>}
                    </div>

                    <UsageStatusFlag/>
                </Page>
            </DragDropContext>
        );
    }
}

const rootSelector = createSelector(
    [groupedDirsSelector],
    dirs => [...(dirs[undefined] || [])].sort((a, b) => a.name.localeCompare(b.name, undefined, {sensitivity: 'base'}))
);

const isForceOpenSelector = createSelector(
    [filteredSelector],
    state => state.isForceOpen
);

export const ScriptRegistry = connect(
    (state) => ({
        directories: rootSelector(state),
        isForceOpen: isForceOpenSelector(state),
        filter: filterSelector(state),
        isScriptUsageReady: state.scriptUsage.ready
    }),
    {
        ...RegistryActionCreators,
        updateFilter: FilterActionCreators.updateFilter
    }
)(ScriptRegistryInternal);
