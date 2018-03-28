import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';


export class ExecutionService {
    getExecutions(isInline, scriptId, onlyLast) {
        return ajaxGet(`${getPluginBaseUrl()}/execution/${isInline ? 'forInline' : 'forRegistry'}/${scriptId}${onlyLast ? '/last' : ''}`);
    }
}
