//@flow
import type {ChangelogType} from './script/types';


export type IssueEventType = {
    id: number,
    name: string
};

export type ProjectType = any; //todo

export type ScriptEntity = {
    id: number,
    name: string,
    description: ?string,
    errorCount: number,
    scriptBody: string,
    changelogs: Array<ChangelogType>,
};

export type FieldProps = {
    label: string,
    isLabelHidden?: boolean,

    isInvalid?: boolean,
    invalidMessage?: string,

    isRequired?: boolean,
    isDisabled?: boolean
}

export type LoadableFieldProps = {
    isLoading?: boolean
}

export type MutableFieldProps<T> = {
    value: ?T,
    onChange: (?T) => void
}

export type OptMutableFieldProps<T> = {
    value?: ?T,
    onChange?: (?T) => void
}

export type FormFieldProps = {
    name?: string
}

export type MutableTextFieldProps<T, FieldType> = {
    value: T,
    onChange: (SyntheticEvent<FieldType>) => void
}

export type ScriptError = {
    message: string
};

export type SyntaxError = ScriptError & {
    startLine: number,
    endLine: number,
    startColumn: number,
    endColumn: number
};

export type ObjectMap = {[string]: string};

export type VoidCallback = () => void;

export type ErrorDataType = any;

export type ErrorType = {
    response: JQueryXHR & {
        data?: ErrorDataType
    },
    message: string
};

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

export type I18nFunction = (...params: Array<string>) => string;
//export type I18nMessageType = I18nFunction | string;
export type I18nMessages = {[string]: string};
