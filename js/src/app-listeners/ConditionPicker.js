//@flow
import React, {type Node} from 'react';

import {AkFieldRadioGroup} from '@atlaskit/field-radio-group';
import {FieldTextStateless} from '@atlaskit/field-text';

import type {ConditionInputType} from './types';

import {jiraService} from '../service';

import {FieldMessages} from '../i18n/common.i18n';
import {ListenerTypeMessages} from '../i18n/listener.i18n';

import {AsyncLoadingMultiSelect, FormField, FieldError} from '../common/ak';

import type {LoaderOptionType} from '../common/ak/AsyncLoadingMultiSelect';

import type {IssueEventType, ProjectType} from '../common/types';

import './ConditionPicker.less';


const projectsLoader = () => jiraService
    .getAllProjects()
    .then(projects => projects.map((project: ProjectType): LoaderOptionType<number> => {
        return {
            value: parseInt(project.id, 10),
            name: `${project.key} - ${project.name}`
        };
    }));

const eventTypeLoader = () => jiraService
    .getEventTypes()
    .then(types => types.map((type: IssueEventType): LoaderOptionType<number> => {
        return {
            value: type.id,
            name: type.name
        };
    }));

type Props = {
    value: ConditionInputType,
    onChange: (value: ConditionInputType) => void,
    error: any,
    isDisabled?: boolean
};

export class ConditionPicker extends React.Component<Props> {
    _onChange = (property: string) =>
        (val: any) => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: val
            });
        };

    _onTypeChange = (e: SyntheticEvent<HTMLInputElement>) => {
        const {value, onChange} = this.props;

        const type = e.currentTarget.value;

        if (type === 'CLASS_NAME' || type === 'ISSUE') {
            onChange({...value, type});
        }
    };

    _onInputChange = (property: string) =>
        (e: SyntheticEvent<HTMLInputElement>) => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: e.currentTarget.value
            });
        };

    render() {
        const {value, error, isDisabled} = this.props;

        let errorField: * = null;
        let errorMessage: * = null;

        if (error) {
            errorField = error.field;
            errorMessage = error.message;
        }

        let paramEl: ?Node = null;

        if (value.type) {
            switch (value.type) {
                case 'CLASS_NAME':
                    paramEl = (
                        <FormField
                            label={ListenerTypeMessages.CLASS_NAME}
                            isRequired={true}

                            isInvalid={errorField === 'condition.className'}
                            invalidMessage={errorField === 'condition.className' ? errorMessage : null}
                        >
                            <FieldTextStateless
                                key="className"
                                shouldFitContainer={true}
                                disabled={isDisabled}

                                value={value.className || ''}
                                onChange={this._onInputChange('className')}
                            />
                        </FormField>
                    );
                    break;
                case 'ISSUE':
                    paramEl = [
                        <AsyncLoadingMultiSelect
                            key="project"
                            label={FieldMessages.projects}
                            isDisabled={isDisabled}

                            value={value.projectIds || []}
                            onChange={this._onChange('projectIds')}
                            loader={projectsLoader}
                        />,
                        <AsyncLoadingMultiSelect
                            key="type"
                            label={FieldMessages.eventTypes}
                            isDisabled={isDisabled}

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
            <div className="flex-column" style={{zIndex: 100}}>
                <AkFieldRadioGroup
                    label={FieldMessages.type}
                    isRequired={true}

                    items={[
                        {
                            label: ListenerTypeMessages.ISSUE,
                            value: 'ISSUE',
                            isSelected: value.type === 'ISSUE',
                            isDisabled
                        },
                        {
                            label: ListenerTypeMessages.CLASS_NAME,
                            value: 'CLASS_NAME',
                            isSelected: value.type === 'CLASS_NAME',
                            isDisabled
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
