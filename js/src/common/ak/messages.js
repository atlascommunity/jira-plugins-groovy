//@flow
import * as React from 'react';

import {colors} from '@atlaskit/theme';

import InfoIcon from '@atlaskit/icon/glyph/info';
import ErrorIcon from '@atlaskit/icon/glyph/error';

import './messages.less';


type MessageProps = {
    title: string,
    children?: React.Node
};

export function InfoMessage({title, children}: MessageProps): React.Node {
    return (
        <div className="InfoMessage">
            <div className="Icon">
                <InfoIcon label="info" primaryColor={colors.P300} size="medium"/>
            </div>
            <div className="Body">
                <div className="Title">
                    {title}
                </div>
                {children}
            </div>
        </div>
    );
}

export function ErrorMessage({title, children}: MessageProps): React.Node {
    return (
        <div className="ErrorMessage">
            <div className="Icon">
                <ErrorIcon label="error" primaryColor={colors.R300} size="medium"/>
            </div>
            <div className="Body">
                <div className="Title">
                    {title}
                </div>
                {children}
            </div>
        </div>
    );
}
