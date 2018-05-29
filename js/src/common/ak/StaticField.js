//@flow
import React, {type Node} from 'react';

import {Label} from '@atlaskit/field-base';

import './StaticField.less';


type StaticFieldValueProps = {
    children: Node
};

function StaticFieldValue({children}: StaticFieldValueProps): Node {
    return <div className="static-field">{children}</div>;
}

type StaticFieldProps = {
    label: string,
    isValidationHidden: boolean,
    children: Node
};

export function StaticField({label, children, isValidationHidden}: StaticFieldProps): Node {
    if (isValidationHidden) {
        return (
            <StaticFieldValue>
                {children}
            </StaticFieldValue>
        );
    }

    return (
        <div>
            <Label label={label}/>
            <StaticFieldValue>
                {children}
            </StaticFieldValue>
        </div>
    );
}

StaticField.defaultProps = {
    isValidationHidden: false
};
