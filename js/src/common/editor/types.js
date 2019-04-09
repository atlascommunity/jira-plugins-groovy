//@flow

export type MarkerType = {|
    startRow: number,
    endRow: number,
    startCol: number,
    endCol: number,
    message: string,
    severity: 'error' | 'warning'
|};

export type TypeDoc = {|
    className: string,
    link: ?string
|};

export type ParameterDoc = {|
    type: TypeDoc,
    name: string
|};

export type MethodDoc = {|
    name: string,
    description: ?string,
    returnType: TypeDoc,
    parameters: $ReadOnlyArray<ParameterDoc>
|};

export type ClassDoc = {|
    builtIn: boolean,
    className: string,
    href: ?string,
    description: ?string,
    methods: ?$ReadOnlyArray<MethodDoc>
|};

export type VariableType = {|
    className: string,
    fullClassName: string,
    javaDoc?: string,
    classDoc?: ClassDoc
|};

export type BindingType = {|
    ...VariableType,
    name: string
|};

export type ReturnType = {|
    ...VariableType,
    label?: string,
    optional?: boolean
|};
