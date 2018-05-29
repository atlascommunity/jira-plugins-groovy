//@flow

export type VariableType = {
    className: string,
    fullClassName: string,
    javaDoc?: string
};

export type BindingType = VariableType & {
    name: string
};

export type ReturnType = VariableType & {
    label?: string,
    optional?: boolean
};

export type MarkerType = {
    startRow: number,
    endRow: number,
    startCol: number,
    endCol: number,
    message: string,
    severity?: string
};
