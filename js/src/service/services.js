import {ListenerService} from './listener.service';
import {ConsoleService} from './console.service';
import {RegistryService} from './registry.service';
import {ExecutionService} from './execution.service';
import {JiraService} from './jira.service';


export const listenerService = new ListenerService();
export const consoleService = new ConsoleService();
export const registryService = new RegistryService();
export const executionService = new ExecutionService();
export const jiraService = new JiraService();
//todo: catch unhandled rejected promises
