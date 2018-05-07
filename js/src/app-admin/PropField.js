//@flow
import * as React from 'react';

import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import {ToggleStateless} from '@atlaskit/toggle';
import {Label} from '@atlaskit/field-base';

import {AsyncPicker} from '../common/ak/AsyncPicker';

import {getPluginBaseUrl} from '../service/ajaxHelper';

import type {SingleValueType} from '../common/ak/types';


type SingleValueTypesEnum = 'USER' | 'GROUP' | 'CUSTOM_FIELD' | 'RESOLUTION';

type CommonProps = {
    label: string,
    onChange: (value: any) => void,
};

type Props = (CommonProps & {
    type: SingleValueTypesEnum,
    value: ?SingleValueType,
}) | (CommonProps & {
    type: 'STRING' | 'TEXT' | 'LONG' | 'DOUBLE',
    value: ?string
}) | (CommonProps & {
    type: 'BOOLEAN',
    value: boolean
});

export class PropField extends React.PureComponent<Props> {
    _toggleCallback = (e: Event) => {
        //$FlowFixMe todo: use different event type when it's changed in @atlaskit/toggle
        this.props.onChange(e.currentTarget.checked);
    };

    _textCallback = (e: SyntheticEvent<HTMLInputElement|HTMLTextAreaElement>) => this.props.onChange(e.currentTarget.value);

    render(): React.Node {
        const {label, type, onChange, value} = this.props;

        switch (type) {
            case 'USER': {
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/userPicker`}
                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            }
            case 'GROUP':
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/groupPicker`}
                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            case 'CUSTOM_FIELD':
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/customFieldPicker`}
                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            case 'RESOLUTION':
                return <AsyncPicker
                    label={label}
                    isRequired={true}

                    src={`${getPluginBaseUrl()}/jira-api/resolutionPicker`}
                    onChange={onChange}
                    //$FlowFixMe
                    value={value}
                />;
            case 'STRING':
                return <FieldTextStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}
                    type="text"

                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'TEXT':
                return <FieldTextAreaStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}

                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'LONG':
                return <FieldTextStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}
                    type="number"

                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'DOUBLE':
                return <FieldTextStateless
                    label={label}
                    required={true}
                    shouldFitContainer={true}
                    type="text"

                    pattern="[0-9.]+"
                    //$FlowFixMe
                    value={value || ''}
                    onChange={this._textCallback}
                />;
            case 'BOOLEAN':
                return <div>
                    <Label label={label}/>
                    <ToggleStateless
                        label={label}
                        size="large"

                        //$FlowFixMe
                        isChecked={value || false}
                        onChange={this._toggleCallback}

                        value="true"
                    />
                </div>;
            default:
                return <div>{'Unsupported type'}</div>;
        }
    }
}
