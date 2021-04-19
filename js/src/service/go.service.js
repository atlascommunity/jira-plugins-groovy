//@flow
import {ajaxDelete, ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';

import type {GlobalObjectScriptType} from '../app-go/types';
import type {ChangelogType} from '../common/script/types';


export class GlobalObjectService {
    getAllScripts(): Promise<$ReadOnlyArray<GlobalObjectScriptType>> {
        return ajaxGet(`${getPluginBaseUrl()}/go/all`);
    }

    getScript(id: number): Promise<GlobalObjectScriptType> {
        return ajaxGet(`${getPluginBaseUrl()}/go/${id}`);
    }

    createScript(data: any): Promise<GlobalObjectScriptType> {
        return ajaxPost(`${getPluginBaseUrl()}/go`, data);
    }

    updateScript(id: number, data: any): Promise<GlobalObjectScriptType> {
        return ajaxPut(`${getPluginBaseUrl()}/go/${id}`, data);
    }

    deleteScript(id: number): Promise<void> {
        return ajaxDelete(`${getPluginBaseUrl()}/go/${id}`);
    }

    restoreScript(id: number): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/go/${id}/restore`);
    }

    getChangelogs(id: number): Promise<$ReadOnlyArray<ChangelogType>> {
        return ajaxGet(`${getPluginBaseUrl()}/go/${id}/changelogs`);
    }
}
