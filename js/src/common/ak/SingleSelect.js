//@flow
import * as React from 'react';

import {Label} from '@atlaskit/field-base';
import Select from '@atlaskit/select';

import type {OldSelectItem, OldSelectValue} from './types';

import type {FieldProps, MutableFieldProps} from '../types';


type SingleSelectProps<T: OldSelectValue> = FieldProps & MutableFieldProps<OldSelectItem<T>> & {
    options: Array<OldSelectItem<T>>
};

export class SingleSelect<T: OldSelectValue> extends React.PureComponent<SingleSelectProps<T>> {
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
