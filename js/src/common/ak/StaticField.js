//@flow

import * as React from 'react';

import {Label} from '@atlaskit/field-base';

import {StaticFieldValue} from '../StaticField';


type StaticFieldProps = {
    label: string,
    children: React.Node
};

export function StaticField({label, children}: StaticFieldProps): React.Node {
    return (
        <div>
            <Label label={label}/>
            <StaticFieldValue>
                {children}
            </StaticFieldValue>
        </div>
    );
}
