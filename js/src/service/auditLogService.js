//@flow
import $ from 'jquery';

import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';

import type {AuditLogData} from '../app-audit/types';


export class AuditLogService {
    getAuditLogPage(offset: number, user: $ReadOnlyArray<string>, category: $ReadOnlyArray<string>): Promise<AuditLogData> {
        return ajaxGet(`${getPluginBaseUrl()}/auditLog/all?${$.param({ offset, user, category })}`);
    }
}
