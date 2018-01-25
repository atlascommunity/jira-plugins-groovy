import {ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';


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

    update(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/scheduled/${id}`, data);
    }

    setEnabled(id, enabled) {
        return ajaxPost(`${getPluginBaseUrl()}/scheduled/${id}/enabled/${enabled}`);
    }
}
