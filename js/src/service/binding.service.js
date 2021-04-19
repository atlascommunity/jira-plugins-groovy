//@flow
import {ajaxGet, getPluginBaseUrl} from './ajaxHelper';

import type {ClassDoc} from '../common/editor/types';


export class BindingService {
    getGlobalBindingTypes(): Promise<{string: ClassDoc}> {
        return ajaxGet(`${getPluginBaseUrl()}/binding/all`);
    }
}
