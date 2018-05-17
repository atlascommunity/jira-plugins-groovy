//@flow
declare module 'AJS' {
    declare type FlagParameters = {
        title?: string,
        type?: 'success' | 'info' | 'warning' | 'error',
        body?: string,
        close?: 'auto' | 'manual' | 'never'
    };

    declare type FlagType = {
        close(): void
    };

    declare export default {
        contextPath(): string;
        flag(FlagParameters): FlagType;
        toInit: (() => void) => void;
        $: JQueryStatic;
    };
}
