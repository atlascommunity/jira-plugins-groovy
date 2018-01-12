import React from 'react';
import PropTypes from 'prop-types';

import Select from 'react-select';
import './react-select-common.less';


export class SingleSelect extends React.Component {
    static propTypes = {
        value: PropTypes.string,
        onChange: PropTypes.func,
        options: PropTypes.arrayOf(PropTypes.shape({
            value: PropTypes.any.isRequired,
            label: PropTypes.string.isRequired
        })).isRequired
    };

    render() {
        const {options, value, onChange, ...otherProps} = this.props;

        return (
            <Select value={value} onChange={onChange} options={options} {...otherProps}/>
        );
    }
}
