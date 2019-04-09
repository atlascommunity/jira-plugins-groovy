//@flow
declare module 'AJS' {
    declare type FlagParameters = {
        title?: string,
        type?: 'success' | 'info' | 'warning' | 'error',
        body?: string,
        close?: 'auto' | 'manual' | 'never'
    };

    declare interface Meta {
        get(string): string
    }

    declare type FlagType = {
        close(): void
    };

    declare export default {
        contextPath(): string;
        flag(FlagParameters): FlagType;
        toInit: (() => void) => void;
        $: JQueryStatic;
        Meta: Meta;
    };
}
