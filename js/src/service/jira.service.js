import {ajaxGet, ajaxPost, getBaseUrl, getPluginBaseUrl} from './ajaxHelper';


export class JiraService {
    getAllProjects() {
        return ajaxGet(`${getBaseUrl()}/rest/api/2/project`);
    }

    getEventTypes() {
        return ajaxGet(`${getPluginBaseUrl()}/jira-api/eventType`);
    }

    getAutoCompleteData() {
        return ajaxGet(`${getBaseUrl()}/rest/api/2/jql/autocompletedata`);
    }

    validateQuery(query) {
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
