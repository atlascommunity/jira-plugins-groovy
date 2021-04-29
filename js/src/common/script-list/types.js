//@flow
import type {Element} from 'react';

import {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import type {DeleteI18n} from './DeleteDialog';

import type {ScriptType} from '../script/types';


export type DeleteCallbackType = (id: number, name: string, onConfirm: () => Promise<void>) => void;

export type ScriptLoadFuncType = (id: number) => ScriptType;

export type ScriptComponentProps<T> = {|
    script: T,
    onDelete?: DeleteCallbackType,
    focused?: boolean,
    collapsible?: boolean,
    loadScriptBody?: ScriptLoadFuncType
|};

export type DialogComponentProps = {|
    isNew: boolean,
    id: ?number,
    isChecked?: boolean
|};

export type I18nType = {
    title: string,
    addItem: string,
    noItems: string,
    delete: DeleteI18n
};

export type ScriptForm = {
    name: string,
    description: string,
    scriptBody: string,
    comment: string
};

export type BreadcrumbsType = Array<?Element<typeof BreadcrumbsItem>>;
