//@flow
import type {ScriptType, ScriptId} from '../common/script/types';


export type EntityType = 'script' | 'directory';

export type WatcherCallback = (type: EntityType, id: ScriptId) => void;
export type CreateCallback = (parentId: ScriptId, type: EntityType) => void;
export type EditCallback = (id: ScriptId, type: EntityType) => void;
export type DeleteCallback = (id: ScriptId, type: EntityType, name: string) => void;

export type WorkflowScriptType = 'CONDITION' | 'VALIDATOR' | 'FUNCTION';

export type RegistryScriptType = ScriptType & {
    types: Array<WorkflowScriptType>
};

export type RegistryDirectoryType = {
    id: number,
    name: string,
    scripts: Array<RegistryScriptType>,
    children: Array<RegistryDirectoryType>
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
