//@flow
import React, {type Node} from 'react';

import SectionMessage from '@atlaskit/section-message';


type MessageProps = {
    title?: string,
    children?: Node
};

export function InfoMessage(props: MessageProps): Node {
    return (
        <SectionMessage
            className="InfoMessage"
            appearance="info"
            {...props}
        />
    );
}

export function ErrorMessage(props: MessageProps): Node {
    return (
        <SectionMessage
            className="ErrorMessage"
            appearance="error"
            {...props}
        />
    );
}

export function SuccessMessage(props: MessageProps): Node {
    return (
        <SectionMessage
            className="SuccessMessage"
            appearance="confirmation"
            {...props}
        />
    );
}
