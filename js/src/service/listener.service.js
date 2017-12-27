import {ajaxDelete, ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';


export class ListenerService {
    getAllListeners() {
        return ajaxGet(`${getPluginBaseUrl()}/listener/all`);
    }

    getListener(id) {
        return ajaxGet(`${getPluginBaseUrl()}/listener/${id}`);
    }

    createListener(data) {
        return ajaxPost(`${getPluginBaseUrl()}/listener`, data);
    }

    updateListener(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/listener/${id}`, data);
    }

    deleteListener(id) {
        return ajaxDelete(`${getPluginBaseUrl()}/listener/${id}`);
    }
}
