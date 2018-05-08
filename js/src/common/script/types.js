//@flow

export type IssueReference = {
    key: string,
    summary: string
};

//todo: move to common
export type JiraUser = {
    name: string,
    displayName: string,
    avatarUrl?: string
};

export type ChangelogType = {
    id: number,
    comment: string,
    diff?: string,
    templateDiff?: string,
    author: JiraUser,
    date: string,
    issueReferences: Array<IssueReference>
};

export type ExecutionType = {
    id: number,
    scriptId: string,
    time: number,
    success: boolean,
    date: string,
    extraParams: {[string]: string},
    error?: string
};

export type ScriptId = number | string;

export type ScriptType = {
    id: ScriptId,
    name: string,
    description?: ?string,
    scriptBody?: string,
    inline?: boolean,
    changelogs?: Array<ChangelogType>,
    errorCount?: number,
};
