//@flow
import * as React from 'react';

import Select from '@atlaskit/select';
import SelectWrapper from '@atlaskit/select/dist/esm/SelectWrapper';
import {Label} from '@atlaskit/field-base';

import type {OldSelectItem, OldSelectValue} from './types';

import type {FieldProps, LoadableFieldProps, MutableFieldProps} from '../types';


type LookupMapType = Map<OldSelectValue, OldSelectItem>;

function getLookupMap(items: Array<OldSelectItem>): LookupMapType {
    const lookupMap = new Map();
    for (const item of items) {
        lookupMap.set(item.value, item);
    }

    return lookupMap;
}

let i: number = 0;

type MultiSelectProps = FieldProps & LoadableFieldProps & MutableFieldProps<Array<OldSelectValue>> & {
    items: Array<OldSelectItem>,
};

type MultiSelectState = {
    lookupMap: LookupMapType
}

export class MultiSelect extends React.Component<MultiSelectProps, MultiSelectState> {
    i = i++;

    _onChange = (val: Array<OldSelectItem>) => {
        let newVal: Array<OldSelectValue> = [];

        if (val) {
            newVal = val.map(item => item.value);
        }

        this.props.onChange(newVal);
    };

    constructor(props: MultiSelectProps) {
        super(props);

        this.state = {
            lookupMap: getLookupMap(props.items)
        };
    }

    componentWillReceiveProps(props: MultiSelectProps) {
        this.setState({
            lookupMap: getLookupMap(props.items)
        });
    }

    render(): React.Node {
        const {isInvalid, invalidMessage, value} = this.props;
        const {lookupMap} = this.state;

        return (
            <div>
                <Label
                    label={this.props.label}
                    isRequired={this.props.isRequired}
                />
                <SelectWrapper
                    id={`multi-select-${this.i}`}

                    validationState={isInvalid ? 'error' : 'default'}
                    validationMessage={isInvalid ? invalidMessage : undefined}
                >
                    <Select
                        shouldFitContainer={true}
                        isMulti={true}

                        isLoading={this.props.isLoading}
                        options={this.props.items}

                        value={value ? value.map(key => lookupMap.get(key)).filter(e => e) : []}
                        onChange={this._onChange}
                    />
                </SelectWrapper>
            </div>
        );
    }
}
