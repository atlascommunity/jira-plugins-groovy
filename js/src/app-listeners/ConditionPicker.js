import React from 'react';
import PropTypes from 'prop-types';

import {jiraService} from '../service/services';
import {AsyncLoadingMultiSelect} from '../common/AsyncLoadingMultiSelect';

import {ConditionModel} from '../model/listener.model';

import './ConditionPicker.less';
import {FieldMessages} from '../i18n/common.i18n';
import {ListenerTypeMessages} from '../i18n/listener.i18n';


const projectsLoader = () => jiraService
    .getAllProjects()
    .then(projects => projects.map(project => {
        return {
            value: parseInt(project.id, 10),
            name: `${project.key} - ${project.name}`
        };
    }));

const eventTypeLoader = () => jiraService
    .getEventTypes()
    .then(types => types.map(type => {
        return {
            value: type.id,
            name: type.name
        };
    }));

export class ConditionPicker extends React.Component {
    static propTypes = {
        value: ConditionModel.isRequired,
        onChange: PropTypes.func.isRequired,
    };

    _onChange = property => {
        return val => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: val
            });
        };
    };

    _onTypeChange = (val) => () => {
        const {value, onChange} = this.props;

        onChange({
            ...value,
            type: val
        });
    };

    _onInputChange = property => {
        return e => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: e.target.value
            });
        };
    };

    render() {
        const {value} = this.props;

        let paramEl = null;

        if (value.type) {
            switch (value.type) {
                case 'CLASS_NAME':
                    paramEl = <div className="field-group">
                            <label>
                                {ListenerTypeMessages.CLASS_NAME}
                            </label>
                            <input
                                type="text"
                                className="text long-field"

                                value={value.className || ''}
                                onChange={this._onInputChange('className')}
                            />
                        </div>;
                    break;
                case 'ISSUE':
                    paramEl = [
                            <div className="field-group" key="project">
                                <label>
                                    {FieldMessages.projects}
                                </label>
                                <AsyncLoadingMultiSelect
                                    value={value.projectIds || []}
                                    onChange={this._onChange('projectIds')}
                                    loader={projectsLoader}
                                />
                            </div>,
                            <div className="field-group" key="type">
                                <label>
                                    {FieldMessages.eventTypes}
                                </label>
                                <AsyncLoadingMultiSelect
                                    value={value.typeIds || []}
                                    onChange={this._onChange('typeIds')}
                                    loader={eventTypeLoader}
                                />
                            </div>
                        ];
                    break;
                default:
                    paramEl = 'not implemented'; //todo
            }
        }

        return <div className="ConditionPicker">
            <div className="flex-row">
                <fieldset className="group">
                    <legend>
                        <span>{FieldMessages.type}</span>
                    </legend>
                    <div className="radio">
                        <input
                            className="radio"
                            type="radio"
                            name="listener-condition-type"
                            id="listener-condition-type-issue"

                            checked={value.type === 'ISSUE'}
                            onClick={this._onTypeChange('ISSUE')}
                        />
                        <label htmlFor="listener-condition-type-issue">{ListenerTypeMessages.ISSUE}</label>
                    </div>
                    <div className="radio">
                        <input
                            className="radio"
                            type="radio"
                            name="listener-condition-type"
                            id="listener-condition-type-class-name"

                            checked={value.type === 'CLASS_NAME'}
                            onClick={this._onTypeChange('CLASS_NAME')}
                        />
                        <label htmlFor="listener-condition-type-class-name">{ListenerTypeMessages.CLASS_NAME}</label>
                    </div>
                </fieldset>
            </div>
            {paramEl}
        </div>;
    }
}
