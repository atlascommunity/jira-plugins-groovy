//@flow
import {ajaxGet, ajaxPost, getBaseUrl, getPluginBaseUrl} from './ajaxHelper';

import type {ProjectType, IssueEventType} from '../common/types';


export type ValidationResult = {
    total: number
};

export class JiraService {
    getAllProjects(): Promise<Array<ProjectType>> {
        return ajaxGet(`${getBaseUrl()}/rest/api/2/project`);
    }

    getEventTypes(): Promise<Array<IssueEventType>> {
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
