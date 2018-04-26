//@flow

export type ParamType = {
    name: string,
    displayName: string,
    paramType: 'BOOLEAN' | 'STRING' | 'TEXT' | 'LONG' | 'DOUBLE' | 'CUSTOM_FIELD' | 'USER' | 'GROUP'
};

export type ScriptType = 'CONDITION' | 'VALIDATOR' | 'FUNCTION';

export type ScriptDescriptionType = {
    id: number,
    name: string,
    params: ?Array<ParamType>
};
