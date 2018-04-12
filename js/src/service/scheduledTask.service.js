import {ajaxDelete, ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';


export class ScheduledTaskService {
    getAllTasks() {
        return ajaxGet(`${getPluginBaseUrl()}/scheduled/all`);
    }

    get(id) {
        return ajaxGet(`${getPluginBaseUrl()}/scheduled/${id}`);
    }

    create(data) {
        return ajaxPost(`${getPluginBaseUrl()}/scheduled`, data);
    }

    doDelete(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/scheduled/${id}`);
    }

    restore(id) {
        return ajaxPost(`${getPluginBaseUrl()}/scheduled/${id}/restore`);
    }

    update(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/scheduled/${id}`, data);
    }

    setEnabled(id, enabled) {
        return ajaxPost(`${getPluginBaseUrl()}/scheduled/${id}/enabled/${enabled}`);
    }
}
