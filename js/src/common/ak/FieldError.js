//@flow
import * as React from 'react';

import ErrorIcon from '@atlaskit/icon/glyph/error';

import './FieldError.less';


type FieldErrorProps = {
    error: React.Node
};

export function FieldError({error}: FieldErrorProps): React.Node {
    return (
        <div className="FieldError">
            <ErrorIcon label="error icon" role="presentation" />
            {error}
        </div>
    );
}
