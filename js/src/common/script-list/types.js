//@flow
import type {VoidCallback} from '../types';


export type ScriptCallbackType = (id: number) => void;

export type ScriptComponentProps<T> = {
    script: T,
    onEdit: ScriptCallbackType,
};

export type DialogComponentProps = {
    isNew: boolean,
    id: ?number
}

export type FullDialogComponentProps = DialogComponentProps & {
    onClose: VoidCallback
}

export type I18nType = {
    title: string,
    addItem: string,
    noItems: string
};
