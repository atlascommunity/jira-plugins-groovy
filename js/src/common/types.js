//@flow
import * as React from 'react';

import type {ChangelogType} from './script/types';


export type EntityType = 'REGISTRY_SCRIPT' | 'REGISTRY_DIRECTORY' | 'LISTENER' | 'REST' | 'CUSTOM_FIELD' | 'SCHEDULED_TASK' | 'ADMIN_SCRIPT';

export type IssueEventType = {
    id: number,
    name: string
};

export type ProjectType = any; //todo

export type ScriptEntityWithoutChangelogs = {
    id: number,
    name: string,
    description: ?string,
    errorCount?: number,
    scriptBody: string
};

export type ScriptEntity = ScriptEntityWithoutChangelogs & {
    changelogs: Array<ChangelogType>,
};

export type AkFormFieldProps = {
    isValidationHidden: boolean
};

export type FieldProps = {
    label: string,
    isLabelHidden?: boolean,

    isInvalid?: boolean,
    invalidMessage?: React.Node,

    isRequired?: boolean,
    isDisabled?: boolean
};

export type LoadableFieldProps = {
    isLoading?: boolean
};

export type MutableFieldProps<T> = {
    value: ?T,
    onChange: (?T) => void
};

export type OptMutableFieldProps<T> = {
    value?: ?T,
    onChange?: (?T) => void
};

export type FormFieldProps = {
    name?: string
};

export type MutableTextFieldProps<T, FieldType> = {
    value: T,
    onChange: (SyntheticEvent<FieldType>) => void
};

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

export type Page<T> = {
    offset: number,
    limit: number,
    total: number,
    size: number,
    isLast: boolean,
    values: $ReadOnlyArray<T>
};
