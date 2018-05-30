//@flow
import type {DeleteI18n} from './DeleteDialog';

import type {VoidCallback} from '../types';


export type ScriptCallbackType = (id: number) => void;
export type DeleteCallbackType = (id: number, name: string, onConfirm: () => Promise<void>) => void;

export type ScriptComponentProps<T> = {
    script: T,
    onEdit: ScriptCallbackType,
    onDelete: DeleteCallbackType
};

export type DialogComponentProps = {
    isNew: boolean,
    id: ?number
};

export type FullDialogComponentProps = DialogComponentProps & {
    onClose: VoidCallback
};

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
