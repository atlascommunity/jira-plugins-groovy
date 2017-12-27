import React from 'react';
import PropTypes from 'prop-types';

import {MultiSelect} from './MultiSelect';


export class AsyncLoadingMultiSelect extends React.Component {
    static propTypes = {
        value: PropTypes.arrayOf(PropTypes.number).isRequired,
        onChange: PropTypes.func.isRequired,
        loader: PropTypes.func.isRequired
    };

    state = {ready: false};

    componentDidMount() {
        this.setState({ready: false});

        this.props.loader().then(options => this.setState({ready: true, options: options}))
    }

    render() {
        if (!this.state.ready) {
            return <span className="spinner"/>;
        }

        return <MultiSelect options={this.state.options} value={this.props.value} onChange={this.props.onChange}/>
    }
}