//@flow
import React, {type Node} from 'react';

import {Record} from 'immutable';
import type {RecordOf, RecordFactory} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import type {FullDialogComponentProps, ScriptForm} from './types';

import {LoadingSpinner} from '../ak/LoadingSpinner';
import {EditorField} from '../ak/EditorField';

import {getMarkers} from '../error';
import {ErrorMessage} from '../ak/messages';
import {CommonMessages, FieldMessages} from '../../i18n/common.i18n';
import {Bindings} from '../bindings';
import {ItemActionCreators} from '../redux';

import type {ItemType} from '../redux';
import type {ReturnType} from '../editor/types';


const bindings = [ Bindings.currentUser ];

type ErrorType = {
    field: ?string,
    messages: ?Array<string>,
    error?: any
};

export type SubmitResult = {
    success: true,
    item: ItemType
} | {
    success: false,
    error: any
};

type ValuesType = RecordOf<ScriptForm>;
type DataType = {[string]: any};

export type ProvidedState = {
    values: ValuesType,
    name: ?string
};

const {addItem, updateItem} = ItemActionCreators;

type Props = FullDialogComponentProps & {
    defaultLoader: () => Promise<ProvidedState>,
    editLoader: (id: number) => Promise<ProvidedState>,
    onSubmit: (id: ?number, data: DataType) => Promise<SubmitResult>,
    addItem: typeof addItem,
    updateItem: typeof updateItem,
    i18n: {
        editTitle: string,
        createTitle: string
    },
    valuesTransformer: (values: ValuesType) => DataType,
    modalProps?: any,
    returnTypes?: $ReadOnlyArray<ReturnType>
};

type State = {
    values: ValuesType,
    isLoadingState: boolean,
    isSubmitting: boolean,
    error: ?ErrorType,
    name: ?string,
};

export const makeScriptForm: RecordFactory<ScriptForm> = Record({
    name: '',
    description: '',
    scriptBody: '',
    comment: ''
});

type ScriptFormField = $Keys<ScriptForm>;

export class ScriptDialog extends React.PureComponent<Props, State> {
    static defaultProps = {
        //$FlowFixMe todo: flow issue?
        valuesTransformer: (values: ValuesType): DataType => values.toJS()
    };

    state = {
        values: makeScriptForm(),
        isLoadingState: true,
        isSubmitting: false,
        error: null,
        name: null
    };

    mutateValue = (field: ScriptFormField, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setTextValue = (field: ScriptFormField) => (event: SyntheticEvent<HTMLInputElement|HTMLTextAreaElement>) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: ScriptFormField) => (value: any) => this.mutateValue(field, value);

    _init = () => {
        const {id, isNew, defaultLoader, editLoader} = this.props;

        this.setState({ isLoadingState: true });

        const promise: * = (!isNew && id) ? editLoader(id) : defaultLoader();

        promise
            .then(({values, name}) =>
                this.setState({
                    values, name,
                    isLoadingState: false,
                    isSubmitting: false,
                    error: null
                })
            );
    };

    _onSubmit = () => {
        const {id, isNew, onClose, onSubmit, valuesTransformer, addItem, updateItem} = this.props;
        const {values} = this.state;

        this.setState({ isSubmitting: true });

        onSubmit(id, valuesTransformer(values))
            .then(
                (result: SubmitResult) => {
                    if (result.success) {
                        if (isNew) {
                            addItem(result.item);
                        } else {
                            updateItem(result.item);
                        }
                        onClose();
                    } else {
                        this.setState({
                            isSubmitting: false,
                            error: result.error
                        });
                    }
                }
            );
    };

    componentDidMount() {
        this._init();
    }

    componentDidUpdate(prevProps: Props) {
        if (prevProps.isNew !== this.props.isNew && prevProps.id !== this.props.id) {
            this._init();
        }
    }

    render() {
        const {i18n, isNew, returnTypes, modalProps, onClose} = this.props;
        const {values, isLoadingState, isSubmitting, error, name} = this.state;

        let content: Node = null;

        if (isLoadingState) {
            content = <LoadingSpinner/>;
        } else {
            let errorMessage: Node = null;
            let errorField: ?string = null;

            let markers: * = null;

            if (error) {
                if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                    const errors = error.error.filter(e => e);
                    markers = getMarkers(errors);
                    errorMessage = errors
                        .map(error => error.messages)
                        .map(error => <p key={error}>{error}</p>);
                } else {
                    errorMessage = error.messages;
                }
                errorField = error.field;
            }

            content = (
                <div className="flex-column">
                    {error && !errorField && errorMessage && <ErrorMessage title={errorMessage}/>}

                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}
                        disabled={isSubmitting}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={values.get('name') || ''}
                        onChange={this._setTextValue('name')}
                    />

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        minimumRows={5}
                        disabled={isSubmitting}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorField === 'description' ? errorMessage : null}

                        label={FieldMessages.description}
                        value={values.get('description') || ''}
                        onChange={this._setTextValue('description')}
                    />

                    <EditorField
                        label={FieldMessages.scriptCode}
                        isRequired={true}
                        isDisabled={isSubmitting}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                        markers={markers}

                        bindings={bindings}
                        returnTypes={returnTypes}

                        value={values.get('scriptBody') || ''}
                        onChange={this._setObjectValue('scriptBody')}
                    />

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        required={!isNew}
                        disabled={isSubmitting}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorField === 'comment' ? errorMessage : null}

                        label={FieldMessages.comment}
                        value={values.get('comment') || ''}
                        onChange={this._setTextValue('comment')}
                    />
                </div>
            );
        }

        return (
            <ModalDialog
                {...modalProps}

                width="x-large"
                scrollBehavior="outside"

                isHeadingMultiline={false}
                heading={isNew ? i18n.createTitle : `${i18n.editTitle}: ${name || ''}`}

                onClose={onClose}
                actions={[
                    {
                        text: isNew ? CommonMessages.create : CommonMessages.update,
                        onClick: this._onSubmit,
                    },
                    {
                        text: CommonMessages.cancel,
                        onClick: onClose,
                    }
                ]}
            >
                {content}
            </ModalDialog>
        );
    }
}
