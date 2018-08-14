//@flow
import React from 'react';

import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {CheckboxStateless, CheckboxGroup} from '@atlaskit/checkbox';
import {gridSize} from '@atlaskit/theme';

import {AsyncPicker} from '../common/ak/AsyncPicker';
import {EditorField} from '../common/ak/EditorField';
import type {SingleValueType} from '../common/ak/types';

import {getPluginBaseUrl} from '../service';


type SingleValueTypesEnum = 'USER' | 'GROUP' | 'CUSTOM_FIELD' | 'RESOLUTION';

type CommonProps = {
    label: string,
    isRequired: boolean,
    onChange: (value: any) => void,
};

type Props = (CommonProps & {
    type: SingleValueTypesEnum,
    value: ?SingleValueType,
}) | (CommonProps & {
    type: 'STRING' | 'TEXT' | 'LONG' | 'DOUBLE' | 'SCRIPT',
    value: ?string
}) | (CommonProps & {
    type: 'BOOLEAN',
    value: boolean
}) | (CommonProps & {
    type: 'MULTI_USER',
    value: ?$ReadOnlyArray<SingleValueType>
});

//todo: fix flow type issues
export class PropField extends React.PureComponent<Props> {
    _toggleCallback = (e: SyntheticEvent<HTMLInputElement>) => this.props.onChange(e.currentTarget.checked);

    _textCallback = (e: SyntheticEvent<HTMLInputElement|HTMLTextAreaElement>) => this.props.onChange(e.currentTarget.value);

    render() {
        const {label, isRequired, type, onChange, value} = this.props;

        switch (type) {
            case 'USER': {
                return <AsyncPicker
                    label={label}
                    isRequired={isRequired}

                    src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                    displayValue={true}

                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            }
            case 'MULTI_USER': {
                return <AsyncPicker
                    label={label}
                    isRequired={isRequired}
                    isMulti={true}

                    src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                    displayValue={true}

                    onChange={onChange}
                    //$FlowFixMe
                    value={value || []}
                />;
            }
            case 'GROUP':
                return <AsyncPicker
                    label={label}
                    isRequired={isRequired}

                    src={`${getPluginBaseUrl()}/jira-api/groupPicker`}
                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            case 'CUSTOM_FIELD':
                return <AsyncPicker
                    label={label}
                    isRequired={isRequired}

                    src={`${getPluginBaseUrl()}/jira-api/customFieldPicker`}
                    displayValue={true}

                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            case 'RESOLUTION':
                return <AsyncPicker
                    label={label}
                    isRequired={isRequired}

                    src={`${getPluginBaseUrl()}/jira-api/resolutionPicker`}
                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            case 'STRING':
                return <FieldTextStateless
                    label={label}
                    required={isRequired}
                    shouldFitContainer={true}
                    type="text"

                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'TEXT':
                return <FieldTextAreaStateless
                    label={label}
                    required={isRequired}
                    shouldFitContainer={true}
                    enableResize="vertical"

                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'LONG':
                return <FieldTextStateless
                    label={label}
                    required={isRequired}
                    shouldFitContainer={true}
                    type="number"

                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'DOUBLE':
                return <FieldTextStateless
                    label={label}
                    required={isRequired}
                    shouldFitContainer={true}
                    type="text"

                    pattern="[0-9.]+"
                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'BOOLEAN':
                return (
                    <div style={{marginTop: `${gridSize()}px`}}>
                        <CheckboxGroup>
                            <CheckboxStateless
                                label={label}
                                name={label}

                                //$FlowFixMe
                                isChecked={value || false}
                                onChange={this._toggleCallback}

                                value="true"
                            />
                        </CheckboxGroup>
                    </div>
                );
            case 'SCRIPT':
                return (
                    <EditorField
                        label={label}
                        isRequired={isRequired}

                        onChange={onChange}
                        //$FlowFixMe
                        value={value}
                    />
                );
            default:
                return <div>{'Unsupported type'}</div>;
        }
    }
}
