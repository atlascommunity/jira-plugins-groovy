//@flow

export type OldSelectValue = string | number;

export type OldSelectItem = {
    +value: OldSelectValue,
    +label: string
};

export type SingleValueType = {
    +value: any,
    +label: string,
    +imgSrc?: string
};
