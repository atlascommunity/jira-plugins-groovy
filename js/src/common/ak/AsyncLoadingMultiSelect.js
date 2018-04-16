import React from 'react';
import PropTypes from 'prop-types';

import {MultiSelect} from './MultiSelect';


function mapOption(option) {
    if (option.content && option.value) {
        return option;
    }
    return {
        value: option.value,
        label: option.name
    };
}

export class AsyncLoadingMultiSelect extends React.Component {
    static propTypes = {
        label: PropTypes.string.isRequired,

        isRequired: PropTypes.bool,

        value: PropTypes.arrayOf(PropTypes.number).isRequired,
        onChange: PropTypes.func.isRequired,
        loader: PropTypes.func.isRequired
    };

    state = {
        options: [],
        ready: false
    };

    componentDidMount() {
        this.setState({ready: false});

        this.props
            .loader()
            .then(options => {
                this.setState({
                    ready: true,
                    options: options.map(mapOption)
                });
            });
    }

    render() {
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
