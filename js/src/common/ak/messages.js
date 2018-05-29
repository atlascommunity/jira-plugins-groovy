//@flow
import React, {type Node} from 'react';

import {colors} from '@atlaskit/theme';

import InfoIcon from '@atlaskit/icon/glyph/info';
import ErrorIcon from '@atlaskit/icon/glyph/error';
import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';

import './messages.less';


type MessageProps = {
    title: Node,
    children?: Node
};

type BaseMessageProps = MessageProps & {
    icon: Node,
    className: string
};

function Message({title, children, icon, className}: BaseMessageProps): Node {
    return (
        <div className={className}>
            <div className="Icon">
                {icon}
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

export function InfoMessage(props: MessageProps): Node {
    return (
        <Message
            className="InfoMessage"
            icon={<InfoIcon label="info" primaryColor={colors.P300} size="medium"/>}
            {...props}
        />
    );
}

export function ErrorMessage(props: MessageProps): Node {
    return (
        <Message
            className="ErrorMessage"
            icon={<ErrorIcon label="error" primaryColor={colors.R300} size="medium"/>}
            {...props}
        />
    );
}

export function SuccessMessage(props: MessageProps): Node {
    return (
        <Message
            className="SuccessMessage"
            icon={<CheckCircleIcon label="error" primaryColor={colors.G300} size="medium"/>}
            {...props}
        />
    );
}
