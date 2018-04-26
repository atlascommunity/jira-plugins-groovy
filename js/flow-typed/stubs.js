declare module 'less' {
    declare module.exports: void;
}

declare module 'ajs' {
    declare module.exports: {
        contextPath: () => string;
        $: JQuery
    };
}

declare module 'external-i18n' {
    declare module.exports: any;
}

declare module 'extDefine' {
    declare module.exports: any; //todo
}