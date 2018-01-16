import React from 'react';
import PropTypes from 'prop-types';

import Select from 'react-select';

import './react-select-common.less';


export class MultiSelect2 extends React.Component {
    static propTypes = {
        value: PropTypes.arrayOf(PropTypes.any),
        onChange: PropTypes.func,
        options: PropTypes.arrayOf(PropTypes.shape({
            value: PropTypes.any.isRequired,
            label: PropTypes.string.isRequired
        })).isRequired
    };

    render() {
        const {value, options, onChange, ...otherProps} = this.props;

        return (
            //todo: properly get value of select
            <Select value={value} options={options} onChange={onChange} multi {...otherProps}/>
        );
    }
}
