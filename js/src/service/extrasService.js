//@flow
import {getPluginBaseUrl, ajaxPost} from './ajaxHelper';


export class ExtrasService {
    clearCache(): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/extras/clearCache`);
    }
}
