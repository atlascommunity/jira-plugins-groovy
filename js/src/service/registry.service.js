import {ajaxDelete, ajaxGet, ajaxPut, ajaxPost, getPluginBaseUrl} from './ajaxHelper';


export class RegistryService {
    getAllDirectories() {
        return ajaxGet(`${getPluginBaseUrl()}/repository/directory/all`);
    }

    getDirectory(id) {
        return ajaxGet(`${getPluginBaseUrl()}/repository/directory/${id}`);
    }

    createDirectory(data) {
        return ajaxPost(`${getPluginBaseUrl()}/repository/directory`, data);
    }

    updateDirectory(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/repository/directory/${id}`, data);
    }

    deleteDirectory(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/repository/directory/${id}`);
    }

    getScript(id) {
        return ajaxGet(`${getPluginBaseUrl()}/repository/script/${id}`);
    }

    createScript(data) {
        return ajaxPost(`${getPluginBaseUrl()}/repository/script/`, data);
    }

    updateScript(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/repository/script/${id}`, data);
    }

    deleteScript(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/repository/script/${id}`);
    }
}