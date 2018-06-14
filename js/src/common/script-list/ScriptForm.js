//@flow
import React, {type Node} from 'react';

import {Link} from 'react-router-dom';

import {Record} from 'immutable';
import type {RecordOf, RecordFactory} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import {Field} from '@atlaskit/form';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import type {DialogComponentProps, ScriptForm as ScriptFormType} from './types';

import {LoadingSpinner} from '../ak/LoadingSpinner';
import {EditorField} from '../ak/EditorField';

import {getMarkers} from '../error';
import {ErrorMessage} from '../ak/messages';
import {CommonMessages, FieldMessages} from '../../i18n/common.i18n';
import {Bindings} from '../bindings';
import {ItemActionCreators} from '../redux';

import type {ItemType} from '../redux';
import type {ReturnType} from '../editor/types';

import './ScriptForm.less';


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

type ValuesType = RecordOf<ScriptFormType>;
type DataType = {[string]: any};

export type ProvidedState = {
    values: ValuesType,
    name: ?string
};

const {addItem, updateItem} = ItemActionCreators;

type Props = DialogComponentProps & {
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
    history: any,
    returnTypes?: $ReadOnlyArray<ReturnType>
};

type State = {
    values: ValuesType,
    isLoadingState: boolean,
    isSubmitting: boolean,
    error: ?ErrorType,
    name: ?string,
};

export const makeScriptForm: RecordFactory<ScriptFormType> = Record({
    name: '',
    description: '',
    scriptBody: '',
    comment: ''
});

type ScriptFormField = $Keys<ScriptFormType>;

export class ScriptForm extends React.PureComponent<Props, State> {
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
        const {id, isNew, history, onSubmit, valuesTransformer, addItem, updateItem} = this.props;
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
                        history.push('/');
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
        const {i18n, isNew, returnTypes} = this.props;
        const {values, isLoadingState, isSubmitting, error, name} = this.state;

        let content: Node = null;

        if (isLoadingState) {
            content = <LoadingSpinner/>;
        } else {
            let errorMessage: ?string = null;
            let errorField: ?string = null;

            let markers: * = null;

            if (error) {
                if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                    const errors = error.error.filter(e => e);
                    markers = getMarkers(errors);
                    errorMessage = errors
                        .map(error => error.messages)
                        .map(error => <p key={error}>{error}</p>);
                } else if (error.messages) {
                    errorMessage = error.messages.join('; ');
                }
                errorField = error.field;
            }

            content = (
                <div className="flex-column">
                    {error && !errorField && errorMessage && <ErrorMessage title={errorMessage}/>}

                    <Field
                        label={FieldMessages.name}
                        isRequired={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <FieldTextStateless
                            shouldFitContainer={true}
                            disabled={isSubmitting}

                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                    </Field>

                    <Field
                        label={FieldMessages.description}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            minimumRows={5}
                            disabled={isSubmitting}

                            value={values.get('description') || ''}
                            onChange={this._setTextValue('description')}
                        />
                    </Field>

                    <Field
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <EditorField
                            isDisabled={isSubmitting}

                            markers={markers}

                            bindings={bindings}
                            returnTypes={returnTypes}

                            value={values.get('scriptBody') || ''}
                            onChange={this._setObjectValue('scriptBody')}
                        />
                    </Field>

                    <Field
                        label={FieldMessages.comment}
                        isRequired={!isNew}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorMessage || ''}

                        validateOnChange={false}
                        validateOnBlur={false}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            disabled={isSubmitting}

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    </Field>
                </div>
            );
        }

        return (
            <Page>
                <PageHeader>
                    {isNew ? i18n.createTitle : `${i18n.editTitle}: ${name || ''}`}
                </PageHeader>
                <div className="flex-column">
                    {content}
                    <div className="formButtons">
                        <ButtonGroup>
                            <Button
                                appearance="primary"

                                isDisabled={isSubmitting || isLoadingState}
                                isLoading={isSubmitting}

                                onClick={this._onSubmit}
                            >
                                {CommonMessages.update}
                            </Button>
                            <Button
                                appearance="link"

                                isDisabled={isSubmitting || isLoadingState}

                                component={Link}
                                to="/"
                            >
                                {CommonMessages.cancel}
                            </Button>
                        </ButtonGroup>
                    </div>
                </div>
            </Page>
        );
    }
}
