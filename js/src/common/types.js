//@flow

export type VoidCallback = () => void;

export type ErrorDataType = {
    error: string
} | any;

export type ErrorType = {
    response: JQueryXHR & {
        data?: ErrorDataType
    },
    message: string
};

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

export type I18nMessages = {[string]: string};
