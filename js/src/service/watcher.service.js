import {ajaxDelete, ajaxGet, ajaxPost, getPluginBaseUrl} from './ajaxHelper';


export class WatcherService {
    getAllWatches(type) {
        return ajaxGet(`${getPluginBaseUrl()}/watch/${type}/all`);
    }

    startWatching(type, id) {
        return ajaxPost(`${getPluginBaseUrl()}/watch/${type}/${id}`);
    }

    stopWatching(type, id) {
        return ajaxDelete(`${getPluginBaseUrl()}/watch/${type}/${id}`);
    }
}
