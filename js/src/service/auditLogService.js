import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';


export class AuditLogService {
    getAuditLogPage(offset) {
        return ajaxGet(`${getPluginBaseUrl()}/auditLog/all?offset=${offset}`);
    }
}
