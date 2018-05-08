//@flow
import * as React from 'react';

import {MultiSelect} from './MultiSelect';

import type {OldSelectItem, OldSelectValue} from './types';

import type {FieldProps, MutableFieldProps} from '../types';


export type LoaderOptionType = {
    value: string,
    name: string
};

function mapOption(option: any): OldSelectItem {
    if (option.label && option.value) {
        return (option: OldSelectItem);
    }
    return {
        value: option.value,
        label: option.name
    };
}

type AsyncLoadingMultiSelectProps = FieldProps & MutableFieldProps<$ReadOnlyArray<OldSelectValue>> & {
    loader: () => Promise<Array<LoaderOptionType>>
};

type AsyncLoadingMultiSelectState = {
    ready: boolean,
    options: $ReadOnlyArray<OldSelectItem>
};

export class AsyncLoadingMultiSelect extends React.Component<AsyncLoadingMultiSelectProps, AsyncLoadingMultiSelectState> {
    state = {
        options: [],
        ready: false
    };

    componentDidMount() {
        this.setState({ready: false});

        this.props
            .loader()
            .then((options: Array<LoaderOptionType>) => {
                this.setState({
                    ready: true,
                    options: options.map(mapOption)
                });
            });
    }

    render(): React.Node {
        return <MultiSelect
            label={this.props.label}
            isRequired={this.props.isRequired}
            shouldFitContainer={true}

            isLoading={!this.state.ready}
            items={this.state.options}

            onChange={this.props.onChange}
            value={this.props.value}
        />;
    }
}
