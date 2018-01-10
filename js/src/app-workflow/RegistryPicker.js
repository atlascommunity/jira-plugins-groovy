import React from 'react';
import PropTypes from 'prop-types';

import {registryService} from '../service/services';
import {SingleSelect} from '../common/SingleSelect';
import {UserPicker} from '../common/UserPicker';


function mapScriptToOption(script) {
    return {
        value: script.id.toString(),
        name: script.name
    };
}

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
        //todo: types
        const inputName = `${this.props.fieldName}-${param.name}`;
        switch (param.paramType) {
            case 'USER':
                return <UserPicker name={inputName}/>;
            case 'STRING':
                return <input type="text" className="text" name={inputName}/>;
            case 'TEXT':
                return <textarea className="textarea" name={inputName}/>;
            default:
                return <div>Unsupported type</div>;
        }
    }

    constructor(props) {
        super();

        this.state = {
            ready: false,
            scripts: [],
            value: {
                id: props.initialValue
            }
        };
    }

    componentDidMount() {
        registryService
            .getAllScripts()
            .then(scripts => this.setState({ scripts, ready: true }));
    }

    render() {
        const {ready, scripts, value} = this.state;

        if (!ready) {
            return <span className="aui-icon aui-icon-wait"/>;
        }

        return <div>
            <div className="field-group">
                <label>Script</label>
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
