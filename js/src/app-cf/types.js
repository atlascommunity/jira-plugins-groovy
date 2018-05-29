//@flow
import type {ChangelogType} from '../common/script/types';


export type FieldConfig = {
    uuid: string,
    customFieldName: string,
    contextName: string,
    scriptBody: string,
    changelogs: Array<ChangelogType>,
    cacheable: boolean,
    velocityParamsEnabled: boolean,
    needsTemplate: boolean,
    type: string,
    expectedType: string,
    searcher: ?string,
    template: ?string
};

export type FieldConfigPreviewResult = {
    time: number,
    htmlResult: string
};
