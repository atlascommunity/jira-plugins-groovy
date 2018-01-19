import {ajaxGet, ajaxPut, getPluginBaseUrl} from './ajaxHelper';


export class FieldConfigService {
    getAllConfigs() {
        return ajaxGet(`${getPluginBaseUrl()}/fieldConfig/all`);
    }

    getFieldConfig(id) {
        return ajaxGet(`${getPluginBaseUrl()}/fieldConfig/${id}`);
    }

    updateFieldConfig(id, data) {
        return ajaxPut(`${getPluginBaseUrl()}/fieldConfig/${id}`, data);
    }
}
