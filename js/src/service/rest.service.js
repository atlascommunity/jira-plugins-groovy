import {ajaxDelete, ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';


export class RestService {
    getAllScripts() {
        return ajaxGet(`${getPluginBaseUrl()}/rest/all`);
    }

    getScript(id) {
        return ajaxGet(`${getPluginBaseUrl()}/rest/${id}`);
    }

    createScript(data) {
        return ajaxPost(`${getPluginBaseUrl()}/rest`, data);
    }

    updateScript(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/rest/${id}`, data);
    }

    deleteScript(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/rest/${id}`);
    }

    restoreScript(id) {
        return ajaxPost(`${getPluginBaseUrl()}/rest/${id}/restore`);
    }
}
