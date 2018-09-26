//@flow
import React, {type Node} from 'react';

import {Map} from 'immutable';
import type {Map as MapType} from 'immutable';

import Button from '@atlaskit/button';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {Checkbox} from '@atlaskit/checkbox';
import {Label} from '@atlaskit/field-base';
import {gridSize} from '@atlaskit/theme';

import AddIcon from '@atlaskit/icon/glyph/add';

import type {ScriptType, ParamType, ScriptDescriptionType} from './types';

import {SingleSelect} from '../common/ak/SingleSelect';
import {LoadingSpinner} from '../common/ak/LoadingSpinner';
import {registryService, getBaseUrl, getPluginBaseUrl} from '../service';
import {CommonMessages} from '../i18n/common.i18n';
import {AsyncPicker} from '../common/ak/AsyncPicker';
import {EditorField} from '../common/ak/EditorField';
import type {OldSelectItem} from '../common/ak/types';
import type {InputEvent} from '../common/EventTypes';
import {InfoMessage} from '../common/ak/messages';


function mapScriptToOption(script: ScriptDescriptionType): OldSelectItem<number> {
    return {
        value: script.id,
        label: script.name
    };
}

function mapScriptToOptionNullable(script: ?ScriptDescriptionType): ?OldSelectItem<number> {
    if (!script) {
        return null;
    }
    return mapScriptToOption(script);
}

type RegistryPickerProps = {
    type: ScriptType,
    fieldName: string,
    values: ?{[string]: any},
    scriptId: ?number
};

type RegistryPickerState = {
    scripts: $ReadOnlyArray<ScriptDescriptionType>,
    script: ?ScriptDescriptionType,
    values: MapType<string, any>,
    ready: boolean
};

export class RegistryPicker extends React.Component<RegistryPickerProps, RegistryPickerState> {
    _onChange = (value: ?OldSelectItem<number>) => {
        if (value) {
            const val = value.value;
            this.setState(state => ({
                script: state.scripts.find(el => el.id === val)
            }));
        } else {
            this.setState({ script: null });
        }
    };

    _setInputValue = (field: string) => (e: InputEvent) => {
        this._mutateValue(field, e.currentTarget.value);
    };

    _setValue = (field: string) => (value: any) => this._mutateValue(field, value);

    _setToggleValue = (field: string) => (e: SyntheticEvent<HTMLInputElement>) => this._mutateValue(field, e.currentTarget.checked);

    _mutateValue = (field: string, value: any) => this.setState((state: RegistryPickerState): any => {
        return {
            values: state.values.set(field, value)
        };
    });

    _renderParam(param: ParamType, label: string): Node {
        const {values} = this.state;

        const {fieldName} = this.props;
        const paramName = param.name;
        const inputName = `${fieldName}-${paramName}`;
        const value = values.get(paramName);
        const isRequired = !param.optional;

        switch (param.paramType) {
            case 'USER':
                return (
                    <AsyncPicker
                        label={label}
                        isRequired={isRequired}

                        src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                        displayValue={true}

                        name={inputName}
                        onChange={this._setValue(paramName)}
                        value={value}
                    />
                );
            case 'MULTI_USER': {
                return (
                    <AsyncPicker
                        label={label}
                        isRequired={true}
                        isMulti={true}

                        name={inputName}
                        delimiter=";"

                        src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                        displayValue={true}

                        onChange={this._setValue(paramName)}
                        value={value}
                    />
                );
            }
            case 'GROUP':
                return (
                    <AsyncPicker
                        label={label}
                        isRequired={isRequired}

                        src={`${getPluginBaseUrl()}/jira-api/groupPicker`}
                        name={inputName}
                        onChange={this._setValue(paramName)}
                        value={value}
                    />
                );
            case 'CUSTOM_FIELD':
                return (
                    <AsyncPicker
                        label={label}
                        isRequired={isRequired}

                        src={`${getPluginBaseUrl()}/jira-api/customFieldPicker`}
                        displayValue={true}

                        name={inputName}
                        onChange={this._setValue(paramName)}
                        value={value}
                    />
                );
            case 'RESOLUTION':
                return (
                    <AsyncPicker
                        label={label}
                        isRequired={isRequired}

                        src={`${getPluginBaseUrl()}/jira-api/resolutionPicker`}
                        name={inputName}
                        onChange={this._setValue(paramName)}
                        value={value}
                    />
                );
            case 'STRING':
                return (
                    <FieldTextStateless
                        label={label}
                        required={isRequired}
                        shouldFitContainer={true}
                        type="text"

                        name={inputName}
                        value={value || ''}
                        onChange={this._setInputValue(paramName)}
                    />
                );
            case 'TEXT':
                return (
                    <FieldTextAreaStateless
                        label={label}
                        required={isRequired}
                        shouldFitContainer={true}
                        enableResize="vertical"

                        name={inputName}
                        value={value || ''}
                        onChange={this._setInputValue(paramName)}
                    />
                );
            case 'LONG':
                return (
                    <FieldTextStateless
                        label={label}
                        required={isRequired}
                        shouldFitContainer={true}
                        type="number"

                        name={inputName}
                        value={value || ''}
                        onChange={this._setInputValue(paramName)}
                    />
                );
            case 'DOUBLE':
                return (
                    <FieldTextStateless
                        label={label}
                        required={isRequired}
                        shouldFitContainer={true}
                        type="text"

                        pattern="[0-9.]+"
                        name={inputName}
                        value={value || ''}
                        onChange={this._setInputValue(paramName)}
                    />
                );
            case 'BOOLEAN':
                return (
                    <div style={{marginTop: `${gridSize()}px`}}>
                        <Checkbox
                            label={label}

                            isChecked={value || false}
                            onChange={this._setToggleValue(paramName)}

                            name={inputName}
                            value="true"
                        />
                    </div>
                );
            case 'SCRIPT':
                return (
                    <div>
                        <EditorField
                            label={label}
                            isRequired={isRequired}

                            onChange={this._setValue(paramName)}
                            value={value}
                        />
                        <textarea
                            className="hidden"

                            readOnly={true}
                            value={value}
                            name={inputName}
                        />
                    </div>
                );
            default:
                return <div>{'Unsupported type'}</div>;
        }
    }

    state = {
        ready: false,
        script: null,
        scripts: [],
        values: Map()
    };

    componentDidMount() {
        registryService
            .getAllScripts(this.props.type)
            .then((scripts: $ReadOnlyArray<ScriptDescriptionType>) => {
                const {values} = this.props;

                this.setState({
                    scripts,
                    ready: true,
                    values: values ? Map(values) : Map(),
                    script: this.props.scriptId ? scripts.find(el => el.id === this.props.scriptId) : null
                });
            });
    }

    render() {
        const {ready, scripts, script} = this.state;

        if (!ready) {
            return <LoadingSpinner/>;
        }

        return (<div className="flex-column">
            <Label
                label={CommonMessages.script}
                isRequired={true}
            />
            <div className="flex-row full-width" style={{zIndex: 2}}>
                <div className="flex-grow">
                    <SingleSelect
                        options={scripts.map(mapScriptToOption)}
                        onChange={this._onChange}
                        value={mapScriptToOptionNullable(script)}

                        name={this.props.fieldName}

                        label=""
                        isLabelHidden={true}
                        shouldFitContainer={true}
                    />
                </div>
                <div className="flex-vertical-middle" style={{marginLeft: '10px'}}>
                    <Button
                        iconBefore={<AddIcon label="add"/>}
                        href={`${getBaseUrl()}/plugins/servlet/my-groovy/registry/script/create`}
                    />
                </div>
            </div>

            {script && script.description &&
                <div style={{marginTop: '8px'}}>
                    <InfoMessage>
                        <pre>
                            {script.description}
                        </pre>
                    </InfoMessage>
                </div>
            }

            {script && script.params &&
                script.params.map(param =>
                    <div key={param.name} style={{zIndex: 1}}>
                        {this._renderParam(param, param.displayName)}
                    </div>
                )
            }
        </div>);
    }
}
