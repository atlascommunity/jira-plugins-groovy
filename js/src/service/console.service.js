//@flow
import {ajaxPost, getPluginBaseUrl} from './ajaxHelper';

import type {ConsoleResult} from '../app-console/types';


export class ConsoleService {
    executeScript(script: string): Promise<ConsoleResult> {
        return ajaxPost(`${getPluginBaseUrl()}/scripts/execute`, {script});
    }
}
