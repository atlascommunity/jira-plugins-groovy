import {ajaxDelete, ajaxGet, ajaxPut, ajaxPost, getPluginBaseUrl} from './ajaxHelper';


export class RegistryService {
    getAllDirectories() {
        return ajaxGet(`${getPluginBaseUrl()}/registry/directory/all`);
    }

    getDirectory(id) {
        return ajaxGet(`${getPluginBaseUrl()}/registry/directory/${id}`);
    }

    createDirectory(data) {
        return ajaxPost(`${getPluginBaseUrl()}/registry/directory`, data);
    }

    updateDirectory(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/registry/directory/${id}`, data);
    }

    deleteDirectory(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/registry/directory/${id}`);
    }

    getAllScripts() {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/all`);
    }

    getScript(id) {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/${id}`);
    }

    createScript(data) {
        return ajaxPost(`${getPluginBaseUrl()}/registry/script/`, data);
    }

    updateScript(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/registry/script/${id}`, data);
    }

    deleteScript(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/registry/script/${id}`);
    }

    moveScript(id, parentId) {
        return ajaxPut(`${getPluginBaseUrl()}/registry/script/${id}/parent`, { parentId: parentId ? parentId : null });
    }

    getScriptWorkflows(id) {
        return ajaxGet(`${getPluginBaseUrl()}/registry/script/${id}/workflows`);
    }
}
