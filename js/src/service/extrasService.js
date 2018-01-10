import {getPluginBaseUrl, ajaxPost} from './ajaxHelper';


export class ExtrasService {
    clearCache() {
        return ajaxPost(`${getPluginBaseUrl()}/extras/clearCache`);
    }
}
