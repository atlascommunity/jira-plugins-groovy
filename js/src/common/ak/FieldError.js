//@flow
import React, {type Node} from 'react';

import ErrorIcon from '@atlaskit/icon/glyph/error';

import './FieldError.less';


type FieldErrorProps = {
    error: Node
};

export function FieldError({error}: FieldErrorProps): Node {
    return (
        <div className="FieldError">
            <ErrorIcon label="error icon" role="presentation" />
            {error}
        </div>
    );
}
