//@flow
import React, {Fragment, type Node, type ComponentType} from 'react';

import {Prompt} from 'react-router-dom';

import type {RecordOf} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import type {DialogComponentProps, ScriptForm as ScriptFormType} from './types';
import {withRoot} from './breadcrumbs';

import {RouterLink, FormField, CheckedEditorField, LoadingSpinner, ErrorMessage} from '../ak';
import type {StaticCheckScriptType} from '../ak/CheckedEditorField';

import {ScrollToTop} from '../ScrollToTop';
import {addItem, updateItem} from '../redux';
import {Bindings} from '../bindings';
import {CommonMessages, FieldMessages} from '../../i18n/common.i18n';

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

type ValuesType<T: ScriptFormType> = RecordOf<T>;
type DataType = {[string]: any};
type ScriptFormField<T: ScriptFormType> = $Keys<T>;

export type ProvidedState<T: ScriptFormType> = {
    values: ValuesType<T>,
    name: ?string
};

export type AdditionalFieldProps<T: ScriptFormType> = {
    values: ValuesType<T>,
    mutateValue: (string, any) => void
};

type AdditionalField<T: ScriptFormType> = {
    key: string,
    component: ComponentType<AdditionalFieldProps<T>>
};

type Props<T: ScriptFormType> = DialogComponentProps & {
    defaultLoader: () => Promise<ProvidedState<T>>,
    editLoader: (id: number) => Promise<ProvidedState<T>>,
    onSubmit: (id: ?number, data: DataType) => Promise<SubmitResult>,
    addItem: typeof addItem,
    updateItem: typeof updateItem,
    i18n: {
        editTitle: string,
        createTitle: string,
        parentName: string,
    },
    valuesTransformer: (values: ValuesType<T>) => DataType,
    additionalFields: $ReadOnlyArray<AdditionalField<T>>,
    history: any,
    returnTo: string,
    returnTypes?: $ReadOnlyArray<ReturnType>,
    scriptType: StaticCheckScriptType
};

type State<T: ScriptFormType> = {
    values: ValuesType<T>,
    isLoadingState: boolean,
    isSubmitting: boolean,
    isModified: boolean,
    error: ?ErrorType,
    name: ?string,
};

export class ScriptForm<T: ScriptFormType> extends React.PureComponent<Props<T>, State<T>> {
    static defaultProps = {
        //$FlowFixMe: toJS() issue
        valuesTransformer: (values: ValuesType): DataType => values.toJS(),
        additionalFields: []
    };

    constructor(props: *) {
        super(props);

        const defaultState = props.defaultLoader();

        this.state = {
            ...defaultState,
            isLoadingState: true,
            isSubmitting: false,
            isModified: false,
            error: null,
            name: null
        };
    }

    mutateValue = (field: ScriptFormField<T>, value: any) => {
        this.setState((state: State<T>): * => {
            return {
                values: state.values.set(field, value),
                isModified: true
            };
        });
    };

    _setTextValue = (field: ScriptFormField<T>) => (event: SyntheticEvent<HTMLInputElement|HTMLTextAreaElement>) =>
        //$FlowFixMe
        this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: ScriptFormField<T>) => (value: any) =>
        //$FlowFixMe
        this.mutateValue(field, value);

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
                    isModified: false,
                    error: null
                })
            );
    };

    _onSubmit = () => {
        const {id, isNew, history, onSubmit, valuesTransformer, addItem, updateItem, returnTo} = this.props;
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
                        history.push(returnTo);
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

    componentDidUpdate(prevProps: Props<T>) {
        if (prevProps.isNew !== this.props.isNew && prevProps.id !== this.props.id) {
            this._init();
        }
    }

    render() {
        const {i18n, isNew, id, additionalFields, returnTypes, scriptType, returnTo} = this.props;
        const {values, isModified, isLoadingState, isSubmitting, error, name} = this.state;

        let content: Node = null;

        if (isLoadingState) {
            content = <LoadingSpinner/>;
        } else {
            let errorMessage: ?string = null;
            let errorField: ?string = null;

            if (error) {
                if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                    const errors = error.error.filter(e => e);
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

                    <FormField
                        label={FieldMessages.name}
                        isRequired={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorMessage || ''}
                    >
                        <FieldTextStateless
                            shouldFitContainer={true}
                            disabled={isSubmitting}

                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                    </FormField>

                    <FormField
                        label={FieldMessages.description}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorMessage || ''}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            minimumRows={5}
                            disabled={isSubmitting}

                            value={values.get('description') || ''}
                            onChange={this._setTextValue('description')}
                        />
                    </FormField>

                    <FormField
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorMessage || ''}
                    >
                        <CheckedEditorField
                            isDisabled={isSubmitting}

                            scriptType={scriptType}

                            bindings={bindings}
                            returnTypes={returnTypes}

                            value={values.get('scriptBody') || ''}
                            onChange={this._setObjectValue('scriptBody')}
                        />
                    </FormField>

                    {additionalFields.map(field =>
                        <Fragment key={field.key}>
                            <field.component
                                values={values}
                                //$FlowFixMe
                                mutateValue={this.mutateValue}
                            />
                        </Fragment>
                    )}

                    <FormField
                        label={FieldMessages.comment}
                        isRequired={!isNew}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorMessage || ''}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            disabled={isSubmitting}

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    </FormField>
                </div>
            );
        }

        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="registry"
                                    text={i18n.parentName}
                                    href={returnTo}
                                    component={RouterLink}
                                />,
                                name && id ? <BreadcrumbsItem
                                    key="script"
                                    text={name}
                                    href={`${returnTo}${id}/view`}
                                    component={RouterLink}
                                /> : null
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {isNew ? i18n.createTitle : `${i18n.editTitle}: ${name || ''}`}
                </PageHeader>
                <ScrollToTop/>
                <Prompt when={isModified && !isSubmitting} message="Are you sure you want to leave?"/>
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
                                {isNew ? CommonMessages.create : CommonMessages.update}
                            </Button>
                            <Button
                                appearance="link"
                                isDisabled={isSubmitting || isLoadingState}

                                component={RouterLink}
                                href={returnTo}
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
