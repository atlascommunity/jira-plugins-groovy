export const globalBindings = [
    {
        name: 'httpClient',
        className: 'HttpClient',
        fullClassName: 'org.apache.http.client.HttpClient'
    },
    {
        name: 'log',
        className: 'Logger',
        fullClassName: 'org.slf4j.Logger'
    },
    {
        name: 'logger',
        className: 'Logger',
        fullClassName: 'org.slf4j.Logger'
    }
];

export const Bindings = {
    currentUser: {
        name: 'currentUser',
        className: 'ApplicationUser',
        fullClassName: 'com.atlassian.jira.user.ApplicationUser'
    },
    issue: {
        name: 'issue',
        className: 'Issue',
        fullClassName: 'com.atlassian.jira.issue.Issue'
    },
    mutableIssue: {
        name: 'issue',
        className: 'MutableIssue',
        fullClassName: 'com.atlassian.jira.issue.MutableIssue'
    },
    event: {
        name: 'event',
        className: 'Object',
        fullClassName: 'java.lang.Object'
    },
    issueEvent: {
        name: 'event',
        className: 'IssueEvent',
        fullClassName: 'com.atlassian.jira.event.issue.IssueEvent'
    },
    transientVars: {
        name: 'transientVars',
        className: 'Map<String, Object>',
        fullClassName: 'java.util.Map'
    },
    method: {
        name: 'method',
        className: 'String',
        fullClassName: 'java.lang.String'
    },
    uriInfo: {
        name: 'uriInfo',
        className: 'UriInfo',
        fullClassName: 'javax.ws.rs.core.UriInfo'
    },
    body: {
        name: 'body',
        className: 'String',
        fullClassName: 'java.lang.String'
    }
};
