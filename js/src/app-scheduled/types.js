//@flow
import type {ScriptEntity} from '../common/types';
import type {SingleValueType} from '../common/ak/types';

//todo: rename
export type ScheduledTaskTypeType = {
    name: string,
    fields: $ReadOnlyArray<'scriptBody'|'issueJql'|'workflowAction'|'transitionOptions'>
};

export type KeyedScheduledTaskTypeType = {
    key: string,
    name: string,
    fields: $ReadOnlyArray<'scriptBody'|'issueJql'|'workflowAction'|'transitionOptions'>
};

//todo: move somewhere else
export const types: {[string]: ScheduledTaskTypeType} = {
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

export const typeList: $ReadOnlyArray<KeyedScheduledTaskTypeType> = Object
    .keys(types)
    .map((key: string): * => {
        return {
            ...(types[key]),
            key
        };
    });


export type ScheduledTaskTypeEnum = 'BASIC_SCRIPT' | 'ISSUE_JQL_SCRIPT' | 'ISSUE_JQL_TRANSITION' | 'DOCUMENT_ISSUE_JQL_SCRIPT';

export type TransitionOptionsType = {
    skipConditions?: boolean,
    skipValidators?: boolean,
    skipPermissions?: boolean
};

export type RunOutcomeType = 'SUCCESS' | 'UNAVAILABLE' | 'ABORTED' | 'FAILED' | 'NOT_RAN';

export type RunInfoType = {
    startDate: string,
    duration: number,
    outcome: RunOutcomeType,
    message: ?string
};

export type ScheduledTaskType = ScriptEntity & {
    id: number,
    uuid: string,
    type: ScheduledTaskTypeEnum,
    enabled: boolean,
    scheduleExpression: string,
    user: ?SingleValueType,
    issueJql: ?string,
    issueWorkflow: ?SingleValueType,
    issueWorkflowAction: ?SingleValueType,
    transitionOptions: ?TransitionOptionsType,
    lastRunInfo: ?RunInfoType,
    nextRunDate: ?string
};

export type RunNowResultType = {
    time: number,
    runOutcome: RunOutcomeType,
    message: ?string
};
