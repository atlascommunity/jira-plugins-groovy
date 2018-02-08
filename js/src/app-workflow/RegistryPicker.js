import React from 'react';
import PropTypes from 'prop-types';

import {Map} from 'immutable';

import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import {registryService} from '../service/services';
import {SingleSelect} from '../common/ak/SingleSelect';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {CommonMessages} from '../i18n/common.i18n';
import {AsyncPicker} from '../common/ak/AsyncPicker';


function mapScriptToOption(script) {
    if (!script) {
        return null;
    }
    return {
        value: script.id,
        label: script.name
    };
}

export class RegistryPicker extends React.Component {
    static propTypes = {
        type: PropTypes.string.isRequired,
        values: PropTypes.object,
        scriptId: PropTypes.number,
        fieldName: PropTypes.string
    };

    _onChange = (value) => {
        this.setState({
            script: value ? this.state.scripts.find(el => el.id === value.value) : null
        });
    };

    _setInputValue = (field) => (e) => this._mutateValue(field, e.target.value);

    _setValue = (field) => (value) => this._mutateValue(field, value);

    _mutateValue = (field, value) => this.setState((state) => {
        return {
            values: state.values.set(field, value)
        };
    });

    _renderParam(param, label) {
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
            default:
                return <div>{'Unsupported type'}</div>;
        }
    }

    state = {
        ready: false,
        scripts: []
    };

    componentDidMount() {
        registryService
            .getAllScripts(this.props.type)
            .then(scripts =>
                this.setState({
                    scripts,
                    ready: true,
                    values: new Map(this.props.values),
                    script: this.props.scriptId ? scripts.find(el => el.id === this.props.scriptId) : null
                }));
    }

    render() {
        const {ready, scripts, script} = this.state;

        if (!ready) {
            return <span className="aui-icon aui-icon-wait"/>;
        }

        return <div className="flex-column">
            <SingleSelect
                options={scripts.map(mapScriptToOption)}
                onChange={this._onChange}
                value={mapScriptToOption(script)}

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
