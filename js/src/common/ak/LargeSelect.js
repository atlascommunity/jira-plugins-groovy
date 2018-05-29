//@flow
import React from 'react';

import Select, {createFilter} from '@atlaskit/select';


const filterFunction = createFilter();

type State = {
    options: $ReadOnlyArray<any>,
    filter: string
};

export class LargeSelect extends React.PureComponent<any, State> {
    state = {
        options: [],
        filter: ''
    };

    static getDerivedStateFromProps(nextProps: *, prevState: *): * {
        return {
            options: LargeSelect._getOptions(prevState.filter, nextProps)
        };
    }

    static _getOptions = (filter: string, props: *): * => {
        if (!props.options) {
            return [];
        }

        const filteredOptions = props.options.filter(it => filterFunction(it, filter));

        if (filter.length <= 3 && filteredOptions.length > 100) {
            return filteredOptions.slice(0, 100);
        }

        return filteredOptions;
    };

    _onFilterChange = (filter: string) => {
        this.setState(
            {
                options: LargeSelect._getOptions(filter, this.props),
                filter
            },
            () => {
                if (this.props.onInputChange) {
                    this.props.onInputChange(filter);
                }
            }
        );
    };

    render() {
        const {options: ignoreOptions, ...props} = this.props;
        const {options} = this.state;

        return (
            <Select {...props} options={options} onInputChange={this._onFilterChange}/>
        );
    }
}
