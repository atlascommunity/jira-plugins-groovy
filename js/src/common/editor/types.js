//@flow

export type BindingType = {
    name: string,
    className: string,
    fullClassName: string,
    javaDoc?: string
}

export type MarkerType = {
    startRow: number,
    endRow: number,
    startCol: number,
    endCol: number,
    message: string
};
