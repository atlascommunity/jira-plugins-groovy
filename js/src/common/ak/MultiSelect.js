//@flow
import React from 'react';

import Select from '@atlaskit/select';

import {FormField} from './FormField';
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

    componentDidUpdate(prevProps: Props<T>) {
        const {items} = this.props;

        if (prevProps.items !== items) {
            this.setState({
                lookupMap: getLookupMap(items)
            });
        }
    }

    render() {
        const {label, isInvalid, isRequired, isDisabled, invalidMessage, value} = this.props;
        const {lookupMap} = this.state;

        return (
            <FormField
                label={label || ''}
                isRequired={isRequired}

                isInvalid={isInvalid}
                invalidMessage={invalidMessage}
            >
                <Select
                    shouldFitContainer={true}
                    isMulti={true}
                    isDisabled={isDisabled}

                    isLoading={this.props.isLoading}
                    options={this.props.items}

                    styles={{ menu: base => ({ ...base, zIndex: 10 }) }}

                    value={value ? value.map(key => lookupMap.get(key)).filter(e => e) : []}
                    onChange={this._onChange}
                />
            </FormField>
        );
    }
}
