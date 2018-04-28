//@flow
import type {ScriptEntity} from '../common/types';


export type ConditionType = {
    type: 'CLASS_NAME' | 'ISSUE',
    typeIds: Array<number>,
    projectIds: Array<number>,
    className: ?string
};

export type ListenerType = ScriptEntity & {
    uuid: string,
    condition: ConditionType
}
