//@flow
import type {ScriptEntity} from '../common/types';


export type ConditionType = {
    type: 'CLASS_NAME' | 'ISSUE',
    typeIds: $ReadOnlyArray<string>,
    projectIds: $ReadOnlyArray<string>,
    className: ?string
};

export type ConditionInputType = {
    type: ?'CLASS_NAME' | 'ISSUE',
    typeIds: $ReadOnlyArray<string>,
    projectIds: $ReadOnlyArray<string>,
    className: ?string
};

export type ListenerType = ScriptEntity & {
    uuid: string,
    condition: ConditionType
};

export type ListenerInputType = ScriptEntity & {
    uuid: string,
    condition: ConditionInputType
};
