import React from 'react';
import PropTypes from 'prop-types';

import {registryService} from '../service/services';
import {SingleSelect} from '../common/SingleSelect';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {CommonMessages} from '../i18n/common.i18n';


function mapScriptToOption(script) {
    return {
        value: script.id.toString(),
        name: script.name
    };
}

//todo: keep values
export class RegistryPicker extends React.Component {
    static propTypes = {
        initialValue: PropTypes.number,
        fieldName: PropTypes.string
    };

    _onChange = (e) => {
        this.setState({
            value: this.state.scripts.find(el => el.id.toString() === e.target.value)
        });
    };

    _renderParam(param) {
        const inputName = `${this.props.fieldName}-${param.name}`;
        switch (param.paramType) {
            case 'USER':
                return <aui-select src={`${getPluginBaseUrl()}/jira-api/userPicker`} name={inputName}/>;
            case 'GROUP':
                return <aui-select src={`${getPluginBaseUrl()}/jira-api/groupPicker`} name={inputName}/>;
            case 'CUSTOM_FIELD':
                return <aui-select src={`${getPluginBaseUrl()}/jira-api/customFieldPicker`} name={inputName}/>;
            case 'STRING':
                return <input type="text" className="text" name={inputName}/>;
            case 'TEXT':
                return <textarea className="textarea" name={inputName}/>;
            case 'LONG':
                return <input type="number" className="text" name={inputName}/>;
            case 'DOUBLE':
                return <input type="text" className="text" pattern="[0-9.]+" name={inputName}/>;
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
                    value: this.props.initialValue ? scripts.find(el => el.id === this.props.initialValue) : null
                }));
    }

    render() {
        const {ready, scripts, value} = this.state;

        if (!ready) {
            return <span className="aui-icon aui-icon-wait"/>;
        }

        return <div>
            <div className="field-group">
                <label>{CommonMessages.script}</label>
                <SingleSelect
                    options={scripts.map(mapScriptToOption)}
                    onChange={this._onChange}
                    value={value ? value.id.toString() : ''}

                    name={this.props.fieldName}
                />
            </div>

            {value && value.params &&
                value.params.map(param =>
                    <div className="field-group" key={param.name}>
                        <label>{param.displayName}</label>
                        {this._renderParam(param)}
                    </div>
                )
            }
        </div>;
    }
}
