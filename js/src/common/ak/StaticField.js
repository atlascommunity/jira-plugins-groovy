//@flow
import * as React from 'react';

import {Label} from '@atlaskit/field-base';

import './StaticField.less';


type StaticFieldValueProps = {
    children: React.Node
};

function StaticFieldValue({children}: StaticFieldValueProps): React.Node {
    return <div className="static-field">{children}</div>;
}

type StaticFieldProps = {
    label: string,
    isValidationHidden: boolean,
    children: React.Node
};

export function StaticField({label, children, isValidationHidden}: StaticFieldProps): React.Node {
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
