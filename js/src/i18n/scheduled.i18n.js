//@flow
// eslint-disable-next-line import/no-extraneous-dependencies
import i18n from 'external-i18n';

import type {I18nFunction} from '../common/types';


export const ScheduledTaskMessages: {[string]: string, transitionOption: *, runNowConfirm: I18nFunction, runNowConfirm: I18nFunction, jqlLimitDescription: I18nFunction} = {
    noTasks: i18n.scheduled.noTasks,
    addTask: i18n.scheduled.addTask,
    editTask: i18n.scheduled.editTask,
    deleteTask: i18n.scheduled.deleteTask,
    runAs: i18n.scheduled.runAs,
    runNow: i18n.scheduled.runNow,
    runNowConfirm: i18n.scheduled.runNowConfirm,
    lastRun: i18n.scheduled.lastRun,
    nextRun: i18n.scheduled.nextRun,
    jqlLimitDescription: i18n.scheduled.jqlLimitDescription,
    jqlScriptDescription: i18n.scheduled.jqlScriptDescription,
    scheduleDescription: i18n.scheduled.scheduleDescription,
    transitionOption: {
        skipConditions: i18n.scheduled.transitionOption.skipConditions,
        skipValidators: i18n.scheduled.transitionOption.skipValidators,
        skipPermissions: i18n.scheduled.transitionOption.skipPermissions,
    },
    transitionOptions: i18n.scheduled.transitionOptions,
};
