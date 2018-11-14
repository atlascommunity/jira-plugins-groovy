//@flow
import React from 'react';

import {MultiSelect} from './MultiSelect';

import type {OldSelectItem, OldSelectValue} from './types';

import type {FieldProps, MutableFieldProps} from '../types';


export type LoaderOptionType<T: OldSelectValue> = {
    +value: T,
    +name: string,
    +label?: string
};

type Props<T: OldSelectValue> = {|
    ...FieldProps,
    ...MutableFieldProps<$ReadOnlyArray<OldSelectValue>>,
    loader: () => Promise<Array<LoaderOptionType<T>>>
|};

type State<T: OldSelectValue> = {
    ready: boolean,
    options: $ReadOnlyArray<OldSelectItem<T>>
};


function mapOption<T: OldSelectValue>(option: LoaderOptionType<T>): OldSelectItem<T> {
    if (option.label && option.value) {
        //$FlowFixMe todo
        return option;
    }
    return {
        value: option.value,
        label: option.name
    };
}

//$FlowFixMe todo: https://github.com/facebook/flow/issues/5256
export class AsyncLoadingMultiSelect<T: OldSelectValue = string> extends React.PureComponent<Props<T>, State<T>> {
    state = {
        options: [],
        ready: false
    };

    componentDidMount() {
        this.setState({ready: false});

        this.props
            .loader()
            .then((options: Array<LoaderOptionType<T>>) => {
                this.setState({
                    ready: true,
                    options: options.map(mapOption)
                });
            });
    }

    render() {
        const {label, isRequired, isDisabled, value, onChange} = this.props;
        const {ready, options} = this.state;

        return (
            <MultiSelect
                label={label}
                isRequired={isRequired}
                isDisabled={isDisabled}
                shouldFitContainer={true}

                isLoading={!ready}
                items={options}

                onChange={onChange}
                value={value}
            />
        );
    }
}
