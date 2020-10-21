//@flow
import React, {Fragment} from 'react';

import {connect} from 'react-redux';
import {createSelector} from 'reselect';

import {DragDropContext} from 'react-beautiful-dnd';

import Page from '@atlaskit/page';
import Blanket from '@atlaskit/blanket';
import PageHeader from '@atlaskit/page-header';
import Button from '@atlaskit/button';
import {FieldTextStateless} from '@atlaskit/field-text';
import {Checkbox} from '@atlaskit/checkbox';
import Breadcrumbs from '@atlaskit/breadcrumbs';
import DropdownMenu, {
    DropdownItemGroupRadio,
    DropdownItemRadio,
} from '@atlaskit/dropdown-menu';

import keyBy from 'lodash/keyBy';

import {ScriptDirectory} from './ScriptDirectory';
import {ScriptDirectoryDialog, type DialogParams} from './ScriptDirectoryDialog';
import {
    filteredSelector, filterSelector, groupedDirsSelector,
    loadState, loadUsage, deleteDirectory, deleteScript, moveScript,
    UpdateActionCreators as FilterActionCreators
} from './redux';
import {UsageStatusFlag} from './UsageStatusFlag';

import type {FilterType, RegistryDirectoryType, WorkflowScriptType} from './types';
import {scriptTypes} from './types';
import {Loader} from './Loader';

import {InfoMessage} from '../common/ak/messages';

import {registryService, watcherService} from '../service';

import {CommonMessages, PageTitleMessages, FieldMessages} from '../i18n/common.i18n';
import {RegistryMessages} from '../i18n/registry.i18n';

import './ScriptRegistry.less';
import type {DeleteDialogProps} from '../common/script-list/DeleteDialog';
import {DeleteDialog} from '../common/script-list/DeleteDialog';
import {withRoot} from '../common/script-list/breadcrumbs';

import {LazilyRenderedContext} from '../common/lazyRender';


type Props = {
    directories: $ReadOnlyArray<RegistryDirectoryType>,
    isScriptUsageReady: boolean,
    isForceOpen: boolean,
    filter: FilterType,
    storeLoaded: boolean,
    deleteDirectory: typeof deleteDirectory,
    deleteScript: typeof deleteScript,
    moveScript: typeof moveScript,
    updateFilter: typeof FilterActionCreators.updateFilter,
    loadState: typeof loadState,
    loadUsage: typeof loadUsage,
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

    _setType = (scriptType: ?WorkflowScriptType) => () => this.props.updateFilter({ scriptType });

    componentDidUpdate(prevProps: Props) {
        if (prevProps.directories !== this.props.directories) {
            //trigger scroll event when directory list is changed so all visible lazily rendered elements render
            window.dispatchEvent(new Event('scroll'));
        }
    }

    componentDidMount() {
        const {loadState, loadUsage, storeLoaded} = this.props;
        if (!storeLoaded) {
            Promise
                .all([
                    registryService.getAllDirectories(),
                    registryService.getRegistryScripts(),
                    watcherService.getAllWatches('REGISTRY_SCRIPT'),
                    watcherService.getAllWatches('REGISTRY_DIRECTORY')
                ])
                .then(
                    ([dirs, scripts, scriptWatches, directoryWatches]: *) => {
                        loadState(keyBy(dirs, 'id'), keyBy(scripts, 'id'), scriptWatches, directoryWatches);
                    }
                );

        registryService
            .getAllScriptUsage()
            .then(usage => loadUsage(usage));
        }
    }

    render() {
        const {waiting, directoryDialogProps, deleteScriptProps, deleteDirectoryProps} = this.state;
        const {isScriptUsageReady, isForceOpen, filter, deleteScript, deleteDirectory} = this.props;

        let directories: * = this.props.directories;

        return (
            <Loader>
                <LazilyRenderedContext>
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
                                    <div className="flex-row space-between">
                                        <div>
                                            <FieldTextStateless
                                                isLabelHidden
                                                compact
                                                label="hidden"
                                                placeholder="Filter"
                                                value={filter.name}
                                                onChange={this._onFilterChange}
                                            />
                                        </div>
                                        <DropdownMenu
                                            trigger={
                                                <Fragment>
                                                    {FieldMessages.type}{' '}
                                                    {filter.scriptType ? <strong>{'('}{filter.scriptType}{')'}</strong> : `(${CommonMessages.all})`}
                                                </Fragment>
                                            }
                                            triggerType="button"
                                        >
                                            <DropdownItemGroupRadio id="categories">
                                                <DropdownItemRadio
                                                    id="all"
                                                    onClick={this._setType(null)}
                                                    isSelected={filter.scriptType === null}
                                                >
                                                    All
                                                </DropdownItemRadio>
                                                {scriptTypes.map(type =>
                                                    <DropdownItemRadio
                                                        id={type} key={type}
                                                        onClick={this._setType(type)}
                                                        isSelected={filter.scriptType === type}
                                                    >
                                                        {type}
                                                    </DropdownItemRadio>
                                                )}
                                            </DropdownItemGroupRadio>
                                        </DropdownMenu>
                                        <div className="flex-vertical-middle">
                                            <Checkbox
                                                label={RegistryMessages.onlyUnused}
                                                isDisabled={!isScriptUsageReady}

                                                isChecked={filter.onlyUnused}
                                                onChange={this._toggleUnused}

                                                value="true"
                                                name="onlyUnused"
                                            />
                                        </div>
                                    </div>
                                }
                                breadcrumbs={<Breadcrumbs>{withRoot([])}</Breadcrumbs>}
                            >
                                {PageTitleMessages.registry}
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
                </LazilyRenderedContext>
            </Loader>
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
        isScriptUsageReady: state.scriptUsage.ready,
        storeLoaded: state.ready
    }),
    {
        deleteDirectory, deleteScript, moveScript,
        updateFilter: FilterActionCreators.updateFilter,
        loadState, loadUsage
    }
)(ScriptRegistryInternal);
