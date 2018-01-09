import React from 'react';
import PropTypes from 'prop-types';


export class SingleSelect extends React.Component {
    static propTypes = {
        value: PropTypes.string,
        onChange: PropTypes.func,
        options: PropTypes.arrayOf(PropTypes.shape({
            value: PropTypes.string.isRequired,
            name: PropTypes.string.isRequired
        })).isRequired
    };

    render() {
        const {options, value, onChange} = this.props;

        return (
            <select value={value} onChange={onChange} className="select">
                <option className="hidden"/>
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
