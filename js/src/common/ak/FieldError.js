import React from 'react';

import ErrorIcon from '@atlaskit/icon/glyph/error';

import './FieldError.less';


export function FieldError({error}) {
    return (
        <div className="FieldError">
            <ErrorIcon label="error icon" role="presentation" />
            {error}
        </div>
    );
}
