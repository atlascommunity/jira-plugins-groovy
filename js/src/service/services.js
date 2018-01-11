// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ListenerService} from './listener.service';
import {ConsoleService} from './console.service';
import {RegistryService} from './registry.service';
import {ExecutionService} from './execution.service';
import {JiraService} from './jira.service';
import {ExtrasService} from './extrasService';
import {AuditLogService} from './auditLogService';


export const listenerService = new ListenerService();
export const consoleService = new ConsoleService();
export const registryService = new RegistryService();
export const executionService = new ExecutionService();
export const jiraService = new JiraService();
export const extrasService = new ExtrasService();
export const auditLogService = new AuditLogService();


window.addEventListener('unhandledrejection', function(err) {
    console.error('uncaught error', err);
    AJS.flag({
        title: 'Error occurred', //todo: i18n
        body: err.reason.message,
        type: 'error',
        close: 'manual'
    });
});
