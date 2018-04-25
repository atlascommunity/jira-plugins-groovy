//@flow

import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';

import type {ExecutionType} from '../common/script/types';


export class ExecutionService {
    getExecutions(isInline? : boolean, scriptId : (number|string), onlyLast: boolean) : Promise<Array<ExecutionType>> {
        return ajaxGet(`${getPluginBaseUrl()}/execution/${isInline ? 'forInline' : 'forRegistry'}/${scriptId}${onlyLast ? '/last' : ''}`);
    }
}
