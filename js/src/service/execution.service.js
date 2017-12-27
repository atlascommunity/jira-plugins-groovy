import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';


export class ExecutionService {
    getExecutions(isInline, scriptId) {
        return ajaxGet(`${getPluginBaseUrl()}/execution/${isInline ? 'forInline' : 'forRegistry'}/${scriptId}`);
    }
}
