//@flow
import type {FieldConfig} from '../app-cf/types';


export type FieldConfigItem = FieldConfig & {
    id: number,
    customFieldId: number,
    errorCount: number
};
