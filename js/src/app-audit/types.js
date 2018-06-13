//@flow
import type {EntityType, Page} from '../common/types';
import type {JiraUser} from '../common/script/types';
import type {OldSelectItem} from '../common/ak/types';


export type EntityAction = 'CREATED' | 'UPDATED' | 'DELETED' | 'RESTORED' | 'ENABLED' | 'DISABLED' | 'MOVED';

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
    users: $ReadOnlyArray<OldSelectItem<string>>,
    categories: $ReadOnlyArray<string>
};
