import {ajaxPost, getPluginBaseUrl} from './ajaxHelper';


export class ConsoleService {
    executeScript(script) {
        return ajaxPost(`${getPluginBaseUrl()}/scripts/execute`, {script});
    }
}
