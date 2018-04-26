declare module 'less' {
    declare module.exports: void;
}

declare module 'ajs' {
    declare module.exports: {
        contextPath: () => string;
        $: JQuery
    };
}
