//@flow
import {ajaxGet, ajaxPost, ajaxPut, getPluginBaseUrl} from './ajaxHelper';

import type {FieldConfig, FieldConfigItem, FieldConfigPreviewResult} from '../app-fields/types';


export class FieldConfigService {
    getAllConfigs(): Promise<Array<FieldConfigItem>> {
        return ajaxGet(`${getPluginBaseUrl()}/fieldConfig/all`);
    }

    getFieldConfig(id: number): Promise<FieldConfig> {
        return ajaxGet(`${getPluginBaseUrl()}/fieldConfig/${id}`);
    }

    updateFieldConfig(id: number, data: *): Promise<FieldConfig> {
        return ajaxPut(`${getPluginBaseUrl()}/fieldConfig/${id}`, data);
    }

    preview(id: number, data: *): Promise<FieldConfigPreviewResult> {
        return ajaxPost(`${getPluginBaseUrl()}/fieldConfig/${id}/preview`, data);
    }
}
