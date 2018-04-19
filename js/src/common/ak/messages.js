import React from 'react';
import PropTypes from 'prop-types';

import Flag from '@atlaskit/flag';
import {colors} from '@atlaskit/theme';

import InfoIcon from '@atlaskit/icon/glyph/info';
import ErrorIcon from '@atlaskit/icon/glyph/error';


const propTypes = {
    title: PropTypes.string.isRequired,
    children: PropTypes.node
};

export function InfoMessage({title, children}) {
    return (
        <Flag
            icon={<InfoIcon label="info" primaryColor={colors.P300}/>}
            title={title}
            description={children}
        />
    );
}

InfoMessage.propTypes = propTypes;

export function ErrorMessage({title, children}) {
    return (
        <Flag
            icon={<ErrorIcon label="info" primaryColor={colors.R300}/>}
            title={title}
            description={children}
        />
    );
}

InfoMessage.propTypes = propTypes;
