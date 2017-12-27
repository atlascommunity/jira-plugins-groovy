import {ajaxGet, getBaseUrl, getPluginBaseUrl} from './ajaxHelper';


export class JiraService {
    getAllProjects() {
        return ajaxGet(`${getBaseUrl()}/rest/api/2/project`);
    }

    getEventTypes() {
        return ajaxGet(`${getPluginBaseUrl()}/jira-api/eventType`);
    }
}
