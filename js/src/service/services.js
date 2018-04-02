// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ListenerService} from './listener.service';
import {ConsoleService} from './console.service';
import {RegistryService} from './registry.service';
import {ExecutionService} from './execution.service';
import {JiraService} from './jira.service';
import {ExtrasService} from './extrasService';
import {AuditLogService} from './auditLogService';
import {RestService} from './rest.service';
import {FieldConfigService} from './fieldConfig.service';
import {PreferenceService} from './preference.service';
import {ScheduledTaskService} from './scheduledTask.service';
import {WatcherService} from './watcher.service';

import {ErrorMessages} from '../i18n/common.i18n';


export const listenerService = new ListenerService();
export const consoleService = new ConsoleService();
export const registryService = new RegistryService();
export const executionService = new ExecutionService();
export const jiraService = new JiraService();
export const extrasService = new ExtrasService();
export const auditLogService = new AuditLogService();
export const restService = new RestService();
export const fieldConfigService = new FieldConfigService();
export const preferenceService = new PreferenceService();
export const scheduledTaskService = new ScheduledTaskService();
export const watcherService = new WatcherService();


window.addEventListener('unhandledrejection', function(err) {
    console.error('uncaught error', err);
    AJS.flag({
        title: ErrorMessages.errorOccurred,
        body: err.reason.message,
        type: 'error',
        close: 'manual'
    });
});
