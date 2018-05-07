//@flow

export type ParamTypeEnum = 'BOOLEAN' | 'STRING' | 'TEXT' | 'LONG' | 'DOUBLE' | 'CUSTOM_FIELD' | 'USER' | 'GROUP' | 'RESOLUTION';

export type ParamType = {
    name: string,
    displayName: string,
    paramType: ParamTypeEnum
};

export type ScriptType = 'CONDITION' | 'VALIDATOR' | 'FUNCTION';

export type ScriptDescriptionType = {
    id: number,
    name: string,
    params: ?Array<ParamType>
};
