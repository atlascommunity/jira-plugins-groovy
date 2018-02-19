import React from 'react';
import PropTypes from 'prop-types';

import {Label} from '@atlaskit/field-base';

import Select from '@atlaskit/select';


export class SingleSelect extends React.Component {
    static propTypes = {
        label: PropTypes.string.isRequired,
        isRequired: PropTypes.bool,
        isLabelHidden: PropTypes.bool,
        value: PropTypes.any,
        onChange: PropTypes.func,
        options: PropTypes.arrayOf(PropTypes.shape({
            value: PropTypes.any.isRequired,
            label: PropTypes.string.isRequired
        })).isRequired
    };

    render() {
        const {label, isRequired, isLabelHidden, options, value, onChange, ...props} = this.props;

        return (
            <div>
                <Label label={label} isRequired={isRequired} isHidden={isLabelHidden}/>
                <Select value={value} onChange={onChange} options={options} {...props}/>
            </div>
        );
    }
}
