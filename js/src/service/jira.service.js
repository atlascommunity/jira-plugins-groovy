//@flow
import {ajaxGet, ajaxPost, getBaseUrl, getPluginBaseUrl} from './ajaxHelper';


export type ValidationResult = {
    total: number
};

export class JiraService {
    getAllProjects(): any { //todo
        return ajaxGet(`${getBaseUrl()}/rest/api/2/project`);
    }

    getEventTypes(): any { //todo
        return ajaxGet(`${getPluginBaseUrl()}/jira-api/eventType`);
    }

    validateQuery(query: string): Promise<ValidationResult> {
        return ajaxPost(
            `${getBaseUrl()}/rest/api/2/search`,
            {
                jql: query,
                validateQuery: true,
                maxResults: 0
            }
        );
    }
}
