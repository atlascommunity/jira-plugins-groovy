import React from 'react';
import PropTypes from 'prop-types';

import {AkFieldRadioGroup} from '@atlaskit/field-radio-group';
import {FieldTextStateless} from '@atlaskit/field-text';

import {jiraService} from '../service/services';
import {AsyncLoadingMultiSelect} from '../common/ak/AsyncLoadingMultiSelect';

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
        error: PropTypes.object
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

    _onTypeChange = (e) => {
        const {value, onChange} = this.props;

        onChange({
            ...value,
            type: e.target.value
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
        const {value, error} = this.props;

        let errorField = null;
        let errorMessage = null;

        if (error) {
            errorField = error.field;
            errorMessage = error.message;
        }

        let paramEl = null;

        if (value.type) {
            switch (value.type) {
                case 'CLASS_NAME':
                    paramEl = <FieldTextStateless
                        key="className"

                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'condition.className'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={value.className || ''}
                        onChange={this._onInputChange('className')}
                    />;
                    break;
                case 'ISSUE':
                    paramEl = [
                        <AsyncLoadingMultiSelect
                            key="project"
                            label={FieldMessages.projects}
                            isRequired={true}

                            value={value.projectIds || []}
                            onChange={this._onChange('projectIds')}
                            loader={projectsLoader}
                        />,
                        <AsyncLoadingMultiSelect
                            key="type"
                            label={FieldMessages.eventTypes}
                            isRequired={true}

                            value={value.typeIds || []}
                            onChange={this._onChange('typeIds')}
                            loader={eventTypeLoader}
                        />
                    ];
                    break;
                default:
                    paramEl = 'not implemented'; //todo: ???
            }
        }

        return (
            <div className="flex-column">
                <AkFieldRadioGroup
                    label={FieldMessages.type}
                    isRequired={true}

                    items={[
                        {
                            label: ListenerTypeMessages.ISSUE,
                            value: 'ISSUE',
                            isSelected: value.type === 'ISSUE'
                        },
                        {
                            label: ListenerTypeMessages.CLASS_NAME,
                            value: 'CLASS_NAME',
                            isSelected: value.type === 'CLASS_NAME'
                        }
                    ]}
                    value={value.type}
                    onRadioChange={this._onTypeChange}
                />
                {paramEl}
            </div>
        );
    }
}
