import {ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';


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

    preview(id, data) {
        return ajaxPost(`${getPluginBaseUrl()}/fieldConfig/${id}/preview`, data);
    }
}
