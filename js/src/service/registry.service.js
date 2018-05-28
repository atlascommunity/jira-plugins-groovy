//@flow
import {ajaxDelete, ajaxGet, ajaxPut, ajaxPost, getPluginBaseUrl} from './ajaxHelper';

import type {
    RegistryDirectoryType,
    RegistryScriptType,
    WorkflowType,
    ScriptUsageItems
} from '../app-registry/types';
import type {ScriptDescriptionType, ScriptType} from '../app-workflow/types';
import type {ChangelogType} from '../common/script/types';


export class RegistryService {
    getAllDirectories(): Promise<$ReadOnlyArray<RegistryDirectoryType>> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/directory/all`);
    }

    getRegistryScripts(): Promise<$ReadOnlyArray<RegistryScriptType>> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/all`);
    }

    getDirectory(id: number): Promise<RegistryDirectoryType> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/directory/${id}`);
    }

    createDirectory(data: any): Promise<RegistryDirectoryType> {
        return ajaxPost(`${getPluginBaseUrl()}/registry/directory`, data);
    }

    updateDirectory(id: number, data: any): Promise<RegistryDirectoryType> {
        return ajaxPut(`${getPluginBaseUrl()}/registry/directory/${id}`, data);
    }

    deleteDirectory(id: number): Promise<void> {
        return ajaxDelete(`${getPluginBaseUrl()}/registry/directory/${id}`);
    }

    restoreDirectory(id: number): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/registry/directory/${id}/restore`);
    }

    getAllScripts(type: ScriptType): Promise<$ReadOnlyArray<ScriptDescriptionType>> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/${type}/all`);
    }

    getScript(id: number): Promise<RegistryScriptType> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/${id}`);
    }

    getScriptChangelogs(id: number): Promise<$ReadOnlyArray<ChangelogType>> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/${id}/changelogs`);
    }

    createScript(data: any): Promise<RegistryScriptType> {
        return ajaxPost(`${getPluginBaseUrl()}/registry/script/`, data);
    }

    updateScript(id: number, data: any): Promise<RegistryScriptType> {
        return ajaxPut(`${getPluginBaseUrl()}/registry/script/${id}`, data);
    }

    deleteScript(id: number): Promise<void> {
        return ajaxDelete(`${getPluginBaseUrl()}/registry/script/${id}`);
    }

    restoreScript(id: number): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/registry/script/${id}/restore`);
    }

    moveScript(id: number, parentId: number): Promise<void> {
        return ajaxPut(`${getPluginBaseUrl()}/registry/script/${id}/parent`, { parentId: parentId ? parentId : null });
    }

    getScriptWorkflows(id: number): Promise<$ReadOnlyArray<WorkflowType>> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/${id}/workflows`);
    }

    getAllScriptUsage(): Promise<ScriptUsageItems> {
        return ajaxGet(`${getPluginBaseUrl()}/registry/workflowUsage`);
    }
}
