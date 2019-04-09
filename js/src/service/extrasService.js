//@flow
import {getPluginBaseUrl, ajaxPost} from './ajaxHelper';

import type {SyntaxError} from '../common/types';


export class ExtrasService {
    clearCache(): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/extras/clearCache`);
    }

    checkScript(scriptBody: string, scriptType: string, additionalParams: {[string]: string} = {}): Promise<$ReadOnlyArray<SyntaxError>> {
        return ajaxPost(`${getPluginBaseUrl()}/staticCheck`, { scriptBody, scriptType, additionalParams });
    }
}
