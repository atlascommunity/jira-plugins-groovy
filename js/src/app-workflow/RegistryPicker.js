import React from 'react';
import PropTypes from 'prop-types';

import {Map} from 'immutable';

import {registryService} from '../service/services';
import {SingleSelect} from '../common/SingleSelect';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {CommonMessages} from '../i18n/common.i18n';
import {AsyncPicker} from '../common/ak/AsyncPicker';


function mapScriptToOption(script) {
    return {
        value: script.id,
        label: script.name
    };
}

export class RegistryPicker extends React.Component {
    static propTypes = {
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

    _renderParam(param) {
        const {values} = this.state;
        const {fieldName} = this.props;
        const paramName = param.name;
        const inputName = `${fieldName}-${paramName}`;
        const value = values.get(paramName);
        switch (param.paramType) {
            case 'USER':
                return <AsyncPicker
                    label=""
                    isLabelHidden={true}

                    src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                    name={inputName}
                    onChange={this._setValue(paramName)}
                    value={value}
                    className="long-field"
                />;
            case 'GROUP':
                return <AsyncPicker
                    label=""
                    isLabelHidden={true}

                    src={`${getPluginBaseUrl()}/jira-api/groupPicker`}
                    name={inputName}
                    onChange={this._setValue(paramName)}
                    value={value}
                    className="long-field"
                />;
            case 'CUSTOM_FIELD':
                return <AsyncPicker
                    label=""
                    isLabelHidden={true}

                    src={`${getPluginBaseUrl()}/jira-api/customFieldPicker`}
                    name={inputName}
                    onChange={this._setValue(paramName)}
                    value={value}
                    className="long-field"
                />;
            case 'STRING':
                return <input
                    type="text"
                    className="text long-field"
                    name={inputName}
                    value={value}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'TEXT':
                return <textarea
                    className="textarea long-field"
                    name={inputName}
                    value={value}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'LONG':
                return <input
                    type="number"
                    className="text long-field"
                    name={inputName}
                    value={value}
                    onChange={this._setInputValue(paramName)}
                />;
            case 'DOUBLE':
                return <input
                    type="text"
                    className="text long-field"
                    pattern="[0-9.]+"
                    name={inputName}
                    value={value}
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
            .getAllScripts()
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

        return <div>
            <div className="field-group">
                <label>{CommonMessages.script}</label>
                <SingleSelect
                    options={scripts.map(mapScriptToOption)}
                    onChange={this._onChange}
                    value={script ? script.id.toString() : ''}

                    name={this.props.fieldName}

                    className="long-field"
                />
            </div>

            {script && script.params &&
                script.params.map(param =>
                    <div className="field-group" key={param.name}>
                        <label>{param.displayName}</label>
                        {this._renderParam(param)}
                    </div>
                )
            }
        </div>;
    }
}
