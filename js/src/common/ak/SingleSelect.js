//@flow
import * as React from 'react';

import {Label} from '@atlaskit/field-base';
import Select from '@atlaskit/select';

import type {OldSelectItem} from './types';

import type {FieldProps, MutableFieldProps} from '../types';


type SingleSelectProps = FieldProps & MutableFieldProps<OldSelectItem> & {
    options: Array<OldSelectItem>
}

export class SingleSelect extends React.Component<SingleSelectProps> {
    render(): React.Node {
        const {label, isRequired, isLabelHidden, options, value, onChange, ...props} = this.props;

        return (
            <div>
                <Label label={label} isRequired={isRequired} isHidden={isLabelHidden}/>
                <Select value={value} onChange={onChange} options={options} {...props}/>
            </div>
        );
    }
}
