//@flow
import {getPluginBaseUrl, ajaxPost} from './ajaxHelper';


export class ExtrasService {
    clearCache(): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/extras/clearCache`);
    }

    checkScript(scriptBody: string, scriptType: string): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/staticCheck`, { scriptBody, scriptType, additionalParams: {} });
    }
}
