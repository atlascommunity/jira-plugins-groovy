//@flow
import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';

import type {AuditLogData} from '../app-audit/types';


export class AuditLogService {
    getAuditLogPage(offset: number): Promise<AuditLogData> {
        return ajaxGet(`${getPluginBaseUrl()}/auditLog/all?offset=${offset}`);
    }
}
