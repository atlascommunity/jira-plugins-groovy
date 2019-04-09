//@flow
import type {EntityAction, EntityType, Page} from '../common/types';
import type {JiraUser} from '../common/script/types';
import type {SingleValueType} from '../common/ak/types';


export type AuditLogEntry = {
    id: number,
    date: string,
    action: EntityAction,
    category: EntityType,
    user: JiraUser,
    deleted: boolean,
    scriptId: ?number,
    description: ?string,
    parentName: ?string,
    scriptName: ?string
};

export type AuditLogData = Page<AuditLogEntry>;

export type AuditLogFilterType = {
    users: $ReadOnlyArray<SingleValueType>,
    categories: $ReadOnlyArray<EntityType>,
    actions: $ReadOnlyArray<EntityAction>
};
