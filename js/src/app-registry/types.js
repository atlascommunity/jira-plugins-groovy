//@flow
import type {ScriptEntityWithoutChangelogs} from '../common/types';


export type EntityType = 'script' | 'directory';

export type WatcherCallback = (type: EntityType, id: number) => void;
export type CreateCallback = (parentId: ?number, type: EntityType) => void;
export type EditCallback = (id: number, type: EntityType) => void;
export type DeleteCallback = (id: number, type: EntityType, name: string) => void;

export type WorkflowScriptType = 'CONDITION' | 'VALIDATOR' | 'FUNCTION';

export type RegistryScriptType = ScriptEntityWithoutChangelogs & {
    types: $ReadOnlyArray<WorkflowScriptType>,
    parentName: string,
    directoryId: number
};

export type BasicRegistryDirectoryType = {
    id: number,
    name: string,
    fullName?: string
};

export type RegistryDirectoryType = BasicRegistryDirectoryType & {
    scripts: $ReadOnlyArray<RegistryScriptType>,
    children: $ReadOnlyArray<RegistryDirectoryType>
};

export type WorkflowActionItemType = {
    type: WorkflowScriptType,
    order: number
};

export type WorkflowActionType = {
    id: number,
    name: string,
    items: Array<WorkflowActionItemType>
};

export type WorkflowType = {
    name: string,
    active: boolean,
    hasDraft: boolean,
    actions: Array<WorkflowActionType>
};

export type WorkflowMode = 'live' | 'draft';
