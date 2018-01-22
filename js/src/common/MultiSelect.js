import React from 'react';
import PropTypes from 'prop-types';


export class MultiSelect extends React.Component {
    static propTypes = {
        options: PropTypes.arrayOf(PropTypes.shape({
            value: PropTypes.number.isRequired,
            name: PropTypes.string.isRequired
        })).isRequired,
        value: PropTypes.arrayOf(PropTypes.number).isRequired,
        onChange: PropTypes.func.isRequired
    };

    _handleChange = (e) => {
        this.props.onChange([...e.target.selectedOptions].map(option => parseInt(option.value, 10)));
    };

    render() {
        const {value, options} = this.props;

        return (
            <select multiple size={4} className="multi-select" onChange={this._handleChange} value={value}>
                {options.map(option =>
                    <option
                        key={option.value}
                        value={option.value}
                    >
                        {option.name}
                    </option>
                )}
            </select>
        );
    }
}
