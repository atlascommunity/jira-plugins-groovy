//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import {Record} from 'immutable';
import type {RecordOf, RecordFactory} from 'immutable';

import Button, {ButtonGroup} from '@atlaskit/button';
import {CheckboxStateless, CheckboxGroup} from '@atlaskit/checkbox';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {FieldTextStateless} from '@atlaskit/field-text';

import type {FieldConfig, FieldConfigPreviewResult} from './types';

import {fieldConfigService} from '../service/services';
import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {ConsoleMessages} from '../i18n/console.i18n';
import type {VoidCallback} from '../common/types';
import type {InputEvent} from '../common/EventTypes';


const bindings = [ Bindings.issue ];
const bindingsWithVelocity = [ Bindings.issue, Bindings.velocityParams ];

type Form = {
    description: string,
    comment: string,
    scriptBody: string,
    cacheable: boolean,
    template?: string,
    velocityParamsEnabled: boolean
};

type FormField = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    description: '',
    comment: '',
    scriptBody: '',
    template: '',
    cacheable: true,
    velocityParamsEnabled: false
});

type Props = {
    id: number,
    fieldConfig: FieldConfig,
    onChange: (FieldConfig) => void,
    onCancel: VoidCallback
};

type State = {
    values: RecordOf<Form>,
    previewKey: ?string,
    previewResult: ?FieldConfigPreviewResult,
    error: *
};

export class CustomFieldForm extends React.Component<Props, State> {
    static propTypes = {
        id: PropTypes.number.isRequired,
        fieldConfig: PropTypes.object.isRequired,
        onChange: PropTypes.func.isRequired,
        onCancel: PropTypes.func.isRequired
    };

    constructor(props: Props) {
        super(props);

        const {fieldConfig} = props;

        this.state = {
            values: makeForm({
                scriptBody: fieldConfig.scriptBody,
                cacheable: fieldConfig.cacheable,
                template: fieldConfig.template || '',
                velocityParamsEnabled: fieldConfig.velocityParamsEnabled,
                comment: ''
            }),
            previewKey: '',
            previewResult: null,
            error: null
        };
    }

    _onSubmit = (e: Event) => {
        if (e) {
            e.preventDefault();
        }

        fieldConfigService
            .updateFieldConfig(this.props.id, this.state.values.toJS())
            .then(
                (data) => this.props.onChange(data),
                (error: *) => {
                    const {response} = error;

                    if (response.status === 400) {
                        this.setState({ error: response.data });
                    } else {
                        throw error;
                    }
                }
            );
    };

    _mutateValue = (field: FormField, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setObjectValue = (field: FormField) => (value: any) => this._mutateValue(field, value);

    _setTextValue = (field: FormField) => (event: InputEvent) => this._mutateValue(field, event.currentTarget.value);

    _setToggleValue = (field: FormField) => (e: Event) => {
        //$FlowFixMe
        this._mutateValue(field, e.currentTarget.checked);
    };

    _setTemplate = this._setObjectValue('template');
    _setScript = this._setObjectValue('scriptBody');

    _setPreviewKey = (e: InputEvent) => this.setState({ previewKey: e.currentTarget.value });

    _preview = () => {
        fieldConfigService
            .preview(
                this.props.id,
                {
                    issueKey: this.state.previewKey,
                    configForm: this.state.values.toJS()
                }
            )
            .then(
                previewResult => this.setState({ previewResult })
            );
    };

    render(): React.Node {
        const {fieldConfig} = this.props;
        const {values, error, previewKey, previewResult} = this.state;

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

        const velocityParamsEnabled = values.get('velocityParamsEnabled');
        return (
            <div className="flex-column">
                <CheckboxGroup>
                    <CheckboxStateless
                        label={FieldMessages.cacheable}

                        onChange={this._setToggleValue('cacheable')}
                        isChecked={values.get('cacheable')}
                    />
                    <CheckboxStateless
                        label="Velocity params"

                        onChange={this._setToggleValue('velocityParamsEnabled')}
                        isChecked={velocityParamsEnabled}
                    />
                </CheckboxGroup>
                <EditorField
                    label={FieldMessages.scriptCode}
                    isRequired={true}

                    bindings={velocityParamsEnabled ? bindingsWithVelocity : bindings}

                    value={values.get('scriptBody')}
                    onChange={this._setScript}

                    isInvalid={errorField === 'scriptBody'}
                    invalidMessage={errorField === 'scriptBody' ? errorMessage : null}

                    markers={markers}
                />

                {fieldConfig.needsTemplate &&
                    <EditorField
                        label="Template" //todo
                        isRequired={true}

                        mode="velocity"
                        //todo: bindings={[ Bindings.issue ]}

                        value={values.get('template')}
                        onChange={this._setTemplate}
                    />
                }
                <FieldTextAreaStateless
                    shouldFitContainer={true}
                    required={!!fieldConfig.uuid}

                    isInvalid={errorField === 'comment'}
                    invalidMessage={errorField === 'comment' ? errorMessage : null}

                    label={FieldMessages.comment}
                    value={values.get('comment') || ''}
                    onChange={this._setTextValue('comment')}
                />
                <div style={{marginTop: '10px'}}>
                    <ButtonGroup>
                        <Button appearance="primary" onClick={this._onSubmit}>{CommonMessages.update}</Button>
                        {fieldConfig.uuid ?
                            <Button appearance="link" onClick={this.props.onCancel}>{CommonMessages.cancel}</Button>
                            : undefined
                        }
                    </ButtonGroup>
                </div>

                <div className="flex-column" style={{marginTop: '20px'}}>
                    <FieldTextStateless
                        label="Issue key"

                        value={previewKey || ''}
                        onChange={this._setPreviewKey}
                    />
                    <div style={{marginTop: '10px'}}>
                        <Button appearance="primary" onClick={this._preview}>
                            {CommonMessages.preview}
                        </Button>
                    </div>

                    {previewResult &&
                        <div>
                            {ConsoleMessages.executedIn(previewResult.time.toString())}{':'}

                            <div dangerouslySetInnerHTML={{__html: previewResult.htmlResult}}/>
                        </div>
                    }
                </div>
            </div>
        );
    }
}
