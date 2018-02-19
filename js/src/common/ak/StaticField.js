import React from 'react';
import PropTypes from 'prop-types';

import {Label} from '@atlaskit/field-base';

import {StaticFieldValue} from '../StaticField';


export function StaticField({label, children}) {
    return (
        <div>
            <Label label={label}/>
            <StaticFieldValue>
                {children}
            </StaticFieldValue>
        </div>
    );
}

StaticField.propTypes = {
    label: PropTypes.string.isRequired,
    children: PropTypes.any.isRequired
};
