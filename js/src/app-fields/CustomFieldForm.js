//@flow
import React from 'react';

import {Link, withRouter} from 'react-router-dom';
import {connect} from 'react-redux';

import {Record} from 'immutable';
import type {RecordOf, RecordFactory} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import {CheckboxStateless, CheckboxGroup} from '@atlaskit/checkbox';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {FieldTextStateless} from '@atlaskit/field-text';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import {fieldConfigSelectorFactory} from './selectors';

import type {FieldConfig, FieldConfigPreviewResult} from './types';

import {fieldConfigService} from '../service/services';
import {CommonMessages, ErrorMessages, FieldMessages} from '../i18n/common.i18n';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {FormField, ErrorMessage, InfoMessage} from '../common/ak';
import {ConsoleMessages} from '../i18n/console.i18n';
import type {InputEvent} from '../common/EventTypes';
import {extractShortClassName} from '../common/classNames';
import {ScriptFieldMessages} from '../i18n/cf.i18n';
import {updateItem} from '../common/redux';
import {withRoot} from '../common/script-list/breadcrumbs';
import {RouterLink} from '../common/ak/RouterLink';
import {ScrollToTop} from '../common/ScrollToTop';


const bindings = [ Bindings.issue ];
const bindingsWithVelocity = [ Bindings.issue, Bindings.velocityParams ];

type Form = {
    comment: string,
    scriptBody: string,
    cacheable: boolean,
    compileStatic: boolean,
    template?: string,
    velocityParamsEnabled: boolean
};

type FormFieldType = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    comment: '',
    scriptBody: '',
    template: '',
    cacheable: true,
    compileStatic: true,
    velocityParamsEnabled: false
});

type Props = {
    id: number,
    fieldConfig: FieldConfig,
    history: any,
    updateItem: typeof updateItem
};

type State = {
    values: RecordOf<Form>,
    waiting: boolean,
    waitingPreview: boolean,
    previewKey: ?string,
    previewResult: ?FieldConfigPreviewResult,
    error: *,
    previewError: *
};

export class CustomFieldFormInternal extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);

        const {fieldConfig} = props;

        this.state = {
            values: makeForm({
                scriptBody: fieldConfig.scriptBody,
                cacheable: fieldConfig.cacheable,
                template: fieldConfig.template || '',
                velocityParamsEnabled: fieldConfig.velocityParamsEnabled,
                compileStatic: fieldConfig.compileStatic,
                comment: ''
            }),
            waiting: false,
            waitingPreview: false,
            previewKey: '',
            previewResult: null,
            error: null,
            previewError: null
        };
    }

    _onSubmit = () => {
        const {history} = this.props;

        this.setState({ waiting: true });

        fieldConfigService
            .updateFieldConfig(this.props.id, this.state.values.toJS())
            .then(
                (data: FieldConfig) => {
                    this.props.updateItem(data);
                    history.push('/fields/');
                },
                (error: *) => {
                    const {response} = error;

                    if (response.status === 400) {
                        this.setState({ error: response.data, waiting: false, previewError: null });
                    } else {
                        this.setState({ waiting: false, error: null, previewError: null });
                        throw error;
                    }
                }
            );
    };

    _preview = () => {
        this.setState({ waitingPreview: true });

        fieldConfigService
            .preview(
                this.props.id,
                {
                    issueKey: this.state.previewKey,
                    configForm: this.state.values.toJS()
                }
            )
            .then(
                previewResult => this.setState({ previewResult, waitingPreview: false, previewError: null }),
                (error: *) => {
                    const {response} = error;

                    if (response.status === 400) {
                        this.setState({ previewError: response.data, previewResult: null, waitingPreview: false });
                    } else {
                        this.setState({ waitingPreview: false, previewError: null, previewResult: null });
                        throw error;
                    }
                }
            );
    };

    _mutateValue = (field: FormFieldType, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setObjectValue = (field: FormFieldType) => (value: any) => this._mutateValue(field, value);

    _setTextValue = (field: FormFieldType) => (event: InputEvent) => this._mutateValue(field, event.currentTarget.value);

    _setToggleValue = (field: FormFieldType) => (e: SyntheticEvent<HTMLInputElement>) => {
        this._mutateValue(field, e.currentTarget.checked);
    };

    _setTemplate = this._setObjectValue('template');
    _setScript = this._setObjectValue('scriptBody');

    _setPreviewKey = (e: InputEvent) => this.setState({ previewKey: e.currentTarget.value });

    render() {
        const {fieldConfig} = this.props;
        const {values, error, previewError, waiting, waitingPreview, previewKey, previewResult} = this.state;

        let errorMessage: * = null;
        let errorField: ?string = null;

        let markers: * = null;

        if (error) {
            if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                const errors = error.error.filter(e => e);
                markers = getMarkers(errors);
                errorMessage = errors
                    .map(error => error.message)
                    .map(error => <p key={error}>{error}</p>);
            } else {
                errorMessage = error.message;
            }
            errorField = error.field;
        }

        const returnTypes = [
            {
                className: extractShortClassName(fieldConfig.expectedType),
                fullClassName: fieldConfig.expectedType,
                optional: true
            }
        ];

        const velocityParamsEnabled = values.get('velocityParamsEnabled');

        //todo: header actions
        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="fields"
                                    text="Scripted fields"
                                    href="/fields"
                                    component={RouterLink}
                                />,
                                <BreadcrumbsItem
                                    key="script"
                                    text={`${fieldConfig.customFieldName} - ${fieldConfig.contextName}`}
                                    href={`/fields/${fieldConfig.id}/view`}
                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {ScriptFieldMessages.scriptFor(`${fieldConfig.customFieldName} - ${fieldConfig.contextName}`)}
                </PageHeader>
                <ScrollToTop/>

                <div className="flex-column">
                    <FormField
                        label={FieldMessages.options}

                        isInvalid={errorField === 'options'}
                        invalidMessage={errorMessage || ''}
                    >
                        <CheckboxGroup>
                            <CheckboxStateless
                                label={FieldMessages.cacheable}

                                isDisabled={waiting}

                                onChange={this._setToggleValue('cacheable')}
                                isChecked={values.get('cacheable')}

                                name="cacheable"
                                value="true"
                            />
                            <CheckboxStateless
                                label="Static compilation"

                                isDisabled={waiting}

                                onChange={this._setToggleValue('compileStatic')}
                                isChecked={values.get('compileStatic')}

                                name="compileStatic"
                                value="true"
                            />
                            {fieldConfig.needsTemplate &&
                                <CheckboxStateless
                                    label="Velocity params"

                                    isDisabled={waiting}

                                    onChange={this._setToggleValue('velocityParamsEnabled')}
                                    isChecked={velocityParamsEnabled}

                                    name="velocityParamsEnabled"
                                    value="true"
                                />
                            }
                        </CheckboxGroup>
                    </FormField>
                    <FormField
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorMessage || ''}
                    >
                        <EditorField
                            bindings={velocityParamsEnabled ? bindingsWithVelocity : bindings}
                            returnTypes={returnTypes}

                            isDisabled={waiting}

                            value={values.get('scriptBody')}
                            onChange={this._setScript}

                            markers={markers}
                        />
                    </FormField>

                    {fieldConfig.needsTemplate &&
                        <FormField
                            label={FieldMessages.template}
                            isRequired={true}

                            isInvalid={errorField === 'template'}
                            invalidMessage={errorMessage || ''}
                        >
                            <EditorField
                                mode="velocity"
                                //todo: bindings={[ Bindings.issue ]}

                                isDisabled={waiting}

                                value={values.get('template')}
                                onChange={this._setTemplate}
                            />
                        </FormField>
                    }
                    <FormField
                        label={FieldMessages.comment}
                        isRequired={!!fieldConfig.uuid}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorMessage || ''}
                    >
                        <FieldTextAreaStateless
                            shouldFitContainer={true}

                            disabled={waiting}

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    </FormField>
                    <FormField
                        label="Preview issue key"
                    >
                        <FieldTextStateless
                            value={previewKey || ''}
                            onChange={this._setPreviewKey}
                            shouldFitContainer={true}
                        />
                    </FormField>
                    <div style={{marginTop: '10px'}}>
                        <ButtonGroup>
                            <Button
                                appearance="primary"

                                isDisabled={waiting || waitingPreview}
                                isLoading={waiting}

                                onClick={this._onSubmit}
                            >
                                {CommonMessages.update}
                            </Button>
                            <Button
                                isDisabled={waiting || waitingPreview}
                                isLoading={waitingPreview}

                                onClick={this._preview}
                            >
                                {CommonMessages.preview}
                            </Button>
                            <Button
                                appearance="link"

                                isDisabled={waiting}

                                component={Link}
                                to="/fields/"
                            >
                                {CommonMessages.cancel}
                            </Button>
                        </ButtonGroup>
                    </div>

                    <div className="flex-column" style={{marginTop: '20px'}}>
                        {previewResult &&
                            <InfoMessage title={ConsoleMessages.executedIn(previewResult.time.toString())}>
                                <div dangerouslySetInnerHTML={{__html: previewResult.htmlResult}}/>
                            </InfoMessage>
                        }
                        {previewError &&
                            <ErrorMessage title={ErrorMessages.errorOccurred}>
                                {previewError.messages.map((e, i) => <div key={i}>{e}</div>)}
                            </ErrorMessage>
                        }
                    </div>
                </div>
            </Page>
        );
    }
}

export const CustomFieldForm =
    withRouter(
        connect(
            (): * => {
                const fieldConfigSelector = fieldConfigSelectorFactory();
                //$FlowFixMe
                return (state, props) => ({
                    fieldConfig: fieldConfigSelector(state, props)
                });
            },
            {
                updateItem: updateItem
            }
        )(CustomFieldFormInternal)
    );
