import React from 'react';
import PropTypes from 'prop-types';

import Flag from '@atlaskit/flag';
import {colors} from '@atlaskit/theme';

import InfoIcon from '@atlaskit/icon/glyph/info';


export function InfoMessage({title}) {
    return (
        <Flag
            icon={<InfoIcon label="info" primaryColor={colors.P300}/>}
            title={title}
        />
    );
}

InfoMessage.propTypes = {
    title: PropTypes.string.isRequired
};
