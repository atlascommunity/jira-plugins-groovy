//@flow
import React from 'react';

import Select from '@atlaskit/select';
import SelectWrapper from '@atlaskit/select/dist/esm/SelectWrapper';
import {Label} from '@atlaskit/field-base';

import type {OldSelectItem, OldSelectValue} from './types';

import type {FieldProps, LoadableFieldProps, MutableFieldProps} from '../types';


type LookupMapType<T> = Map<OldSelectValue, OldSelectItem<T>>;

function getLookupMap<T: OldSelectValue>(items: $ReadOnlyArray<OldSelectItem<T>>): LookupMapType<T> {
    const lookupMap = new Map();
    for (const item of items) {
        lookupMap.set(item.value, item);
    }

    return lookupMap;
}

let i: number = 0;

type Props<T: OldSelectValue> = FieldProps & LoadableFieldProps & MutableFieldProps<$ReadOnlyArray<OldSelectValue>> & {
    items: $ReadOnlyArray<OldSelectItem<T>>,
};

type State<T: OldSelectValue> = {
    lookupMap: LookupMapType<T>
};

export class MultiSelect<T: OldSelectValue> extends React.PureComponent<Props<T>, State<T>> {
    i = i++;

    _onChange = (val: Array<OldSelectItem<T>>) => {
        let newVal: Array<OldSelectValue> = [];

        if (val) {
            newVal = val.map(item => item.value);
        }

        this.props.onChange(newVal);
    };

    constructor(props: Props<T>) {
        super(props);

        this.state = {
            lookupMap: getLookupMap(props.items)
        };
    }

    componentWillReceiveProps(props: Props<T>) {
        this.setState({
            lookupMap: getLookupMap(props.items)
        });
    }

    render() {
        const {isInvalid, invalidMessage, value} = this.props;
        const {lookupMap} = this.state;

        return (
            <div>
                <Label
                    label={this.props.label || ''}
                    isRequired={this.props.isRequired}
                />
                <SelectWrapper
                    id={`multi-select-${this.i}`}

                    validationState={isInvalid ? 'error' : 'default'}
                    //$FlowFixMe
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
