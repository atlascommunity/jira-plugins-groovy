//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import {AkFieldRadioGroup} from '@atlaskit/field-radio-group';
import {FieldTextStateless} from '@atlaskit/field-text';

import type {ConditionType} from './types';

import {jiraService} from '../service/services';

import {ConditionModel} from '../model/listener.model';

import {FieldMessages} from '../i18n/common.i18n';
import {ListenerTypeMessages} from '../i18n/listener.i18n';

import {AsyncLoadingMultiSelect} from '../common/ak/AsyncLoadingMultiSelect';
import {FieldError} from '../common/ak/FieldError';

import type {LoaderOptionType} from '../common/ak/AsyncLoadingMultiSelect';

import type {IssueEventType, ProjectType} from '../common/types';

import './ConditionPicker.less';


const projectsLoader = () => jiraService
    .getAllProjects()
    .then(projects => projects.map((project: ProjectType): LoaderOptionType => {
        return {
            value: project.id,
            name: `${project.key} - ${project.name}`
        };
    }));

const eventTypeLoader = () => jiraService
    .getEventTypes()
    .then(types => types.map((type: IssueEventType): LoaderOptionType => {
        return {
            value: type.id.toString(10),
            name: type.name
        };
    }));

type Props = {
    value: ConditionType,
    onChange: (value: ConditionType) => void,
    error: any
};

export class ConditionPicker extends React.Component<Props> {
    static propTypes = {
        value: ConditionModel.isRequired,
        onChange: PropTypes.func.isRequired,
        error: PropTypes.object
    };

    _onChange = (property: string): * => {
        return (val: any) => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: val
            });
        };
    };

    _onTypeChange = (e: SyntheticEvent<HTMLInputElement>) => {
        const {value, onChange} = this.props;

        const type = e.currentTarget.value;

        if (type === 'CLASS_NAME' || type === 'ISSUE') {
            onChange({...value, type});
        }
    };

    _onInputChange = (property: string): * => {
        return (e: SyntheticEvent<HTMLInputElement>) => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: e.currentTarget.value
            });
        };
    };

    render(): React.Node {
        const {value, error} = this.props;

        let errorField: * = null;
        let errorMessage: * = null;

        if (error) {
            errorField = error.field;
            errorMessage = error.message;
        }

        let paramEl: ?React.Node = null;

        if (value.type) {
            switch (value.type) {
                case 'CLASS_NAME':
                    paramEl = <FieldTextStateless
                        key="className"

                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'condition.className'}
                        invalidMessage={errorField === 'condition.className' ? errorMessage : null}

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

                            value={value.projectIds || []}
                            onChange={this._onChange('projectIds')}
                            loader={projectsLoader}
                        />,
                        <AsyncLoadingMultiSelect
                            key="type"
                            label={FieldMessages.eventTypes}

                            value={value.typeIds || []}
                            onChange={this._onChange('typeIds')}
                            loader={eventTypeLoader}
                        />
                    ];
                    break;
                default:
                    paramEl = 'not implemented';
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
                {errorField === 'condition.type' && <FieldError error={errorMessage}/>}
                {paramEl}
            </div>
        );
    }
}
