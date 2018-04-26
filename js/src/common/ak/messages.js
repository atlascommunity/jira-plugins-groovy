//@flow
import * as React from 'react';

import Flag from '@atlaskit/flag';
import {colors} from '@atlaskit/theme';

import InfoIcon from '@atlaskit/icon/glyph/info';
import ErrorIcon from '@atlaskit/icon/glyph/error';


type MessageProps = {
    title: string,
    children?: React.Node
};

export function InfoMessage({title, children}: MessageProps): React.Node {
    return (
        //$FlowFixMe
        <Flag
            icon={<InfoIcon label="info" primaryColor={colors.P300}/>}
            title={title}
            description={children}
        />
    );
}

export function ErrorMessage({title, children}: MessageProps): React.Node {
    return (
        //$FlowFixMe
        <Flag
            icon={<ErrorIcon label="info" primaryColor={colors.R300}/>}
            title={title}
            description={children}
        />
    );
}
