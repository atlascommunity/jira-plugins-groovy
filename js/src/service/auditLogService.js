//@flow
import $ from 'jquery';

import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';

import type {AuditLogData} from '../app-audit/types';
import type {EntityAction, EntityType} from '../common/types';


export class AuditLogService {
    getAuditLogPage(
        offset: number,
        user: $ReadOnlyArray<string>,
        category: $ReadOnlyArray<EntityType>,
        action: $ReadOnlyArray<EntityAction>
    ): Promise<AuditLogData> {
        return ajaxGet(`${getPluginBaseUrl()}/auditLog/all?${$.param({ offset, user, category, action })}`);
    }
}
