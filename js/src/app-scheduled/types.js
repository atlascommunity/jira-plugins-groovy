export const types = {
    BASIC_SCRIPT: {
        name: 'Basic script',
        fields: ['scriptBody']
    },
    ISSUE_JQL_SCRIPT: {
        name: 'JQL issue script',
        fields: ['issueJql', 'scriptBody']
    },
    DOCUMENT_ISSUE_JQL_SCRIPT: {
        name: 'JQL document issue script',
        fields: ['issueJql', 'scriptBody']
    },
    ISSUE_JQL_TRANSITION: {
        name: 'JQL issue transition',
        fields: ['issueJql', 'workflowAction', 'transitionOptions']
    }
};

export const typeList = Object
    .keys(types)
    .map(key => {
        return {
            ...(types[key]),
            key
        };
    });
