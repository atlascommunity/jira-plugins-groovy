//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import {Map} from 'immutable';
import type {Map as MapType} from 'immutable';

import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {ToggleStateless} from '@atlaskit/toggle';
import {Label} from '@atlaskit/field-base';

import type {ScriptType, ParamType, ScriptDescriptionType} from './types';

import {registryService} from '../service/services';
import {SingleSelect} from '../common/ak/SingleSelect';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {CommonMessages} from '../i18n/common.i18n';
import {AsyncPicker} from '../common/ak/AsyncPicker';
import type {OldSelectItem} from '../common/ak/types';


function mapScriptToOption(script: ScriptDescriptionType): OldSelectItem {
    return {
        value: script.id,
        label: script.name
    };
}

function mapScriptToOptionNullable(script: ?ScriptDescriptionType): ?OldSelectItem {
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
    scripts: Array<ScriptDescriptionType>,
    script: ?ScriptDescriptionType,
    values: MapType<string, any>,
    ready: boolean
};

export class RegistryPicker extends React.Component<RegistryPickerProps, RegistryPickerState> {
    static propTypes = {
        type: PropTypes.string.isRequired,
        values: PropTypes.object,
        scriptId: PropTypes.number,
        fieldName: PropTypes.string
    };

    _onChange = (value: ?OldSelectItem) => {
        if (value) {
            this.setState({
                //$FlowFixMe
                script: this.state.scripts.find(el => el.id === value.value)
            });
        } else {
            this.setState({ script: null });
        }
    };

    _setInputValue = (field: string) => (e: SyntheticEvent<any>) => {
        //$FlowFixMe
        this._mutateValue(field, e.target.value);
    };

    _setValue = (field: string) => (value: any) => this._mutateValue(field, value);

    _setToggleValue = (field: string) => (e: Event) => {
        //$FlowFixMe
        this._mutateValue(field, e.target.checked);
    };

    _mutateValue = (field: string, value: any) => this.setState((state: RegistryPickerState): any => {
        return {
            values: state.values.set(field, value)
        };
    });

    _renderParam(param: ParamType, label: string): React.Node {
        const {values} = this.state;

        const {fieldName} = this.props;
        const paramName = param.name;
        const inputName = `${fieldName}-${paramName}`;
        const value = values.get(paramName);
        switch (param.paramType) {
            case 'USER':
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                    name={inputName}
                    onChange={this._setValue(paramName)}
                    value={value}
                />;
            case 'GROUP':
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/groupPicker`}
                    name={inputName}
                    onChange={this._setValue(paramName)}
                    value={value}
                />;
            case 'CUSTOM_FIELD':
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/customFieldPicker`}
                    name={inputName}
                    onChange={this._setValue(paramName)}
                    value={value}
                />;
            case 'STRING':
                return <FieldTextStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}
                    type="text"

                    name={inputName}
                    value={value || ''}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'TEXT':
                return <FieldTextAreaStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}

                    name={inputName}
                    value={value || ''}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'LONG':
                return <FieldTextStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}
                    type="number"

                    name={inputName}
                    value={value || ''}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'DOUBLE':
                return <FieldTextStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}
                    type="text"

                    pattern="[0-9.]+"
                    name={inputName}
                    value={value || ''}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'BOOLEAN':
                return <div>
                    <Label label={label}/>
                    <ToggleStateless
                        label={label}
                        size="large"

                        isChecked={value || false}
                        onChange={this._setToggleValue(paramName)}

                        name={inputName}
                        value="true"
                    />
                </div>;
            default:
                return <div>{'Unsupported type'}</div>;
        }
    }

    state = {
        ready: false,
        script: null,
        scripts: [],
        values: new Map()
    };

    componentDidMount() {
        registryService
            .getAllScripts(this.props.type)
            .then(scripts =>
                this.setState({
                    scripts,
                    ready: true,
                    //$FlowFixMe
                    values: new Map(this.props.values),
                    script: this.props.scriptId ? scripts.find(el => el.id === this.props.scriptId) : null
                }));
    }

    render(): React.Node {
        const {ready, scripts, script} = this.state;

        if (!ready && !scripts) {
            return <span className="aui-icon aui-icon-wait"/>;
        }

        return <div className="flex-column">
            <SingleSelect
                options={scripts.map(mapScriptToOption)}
                onChange={this._onChange}
                value={mapScriptToOptionNullable(script)}

                name={this.props.fieldName}

                label={CommonMessages.script}
                isRequired={true}
                shouldFitContainer={true}
            />

            {script && script.params &&
                script.params.map(param =>
                    <div key={param.name}>
                        {this._renderParam(param, param.displayName)}
                    </div>
                )
            }
        </div>;
    }
}
