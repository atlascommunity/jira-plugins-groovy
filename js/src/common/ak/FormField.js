//@flow
import React, {type Node} from 'react';

import {Field} from '@atlaskit/form';


type Props = {|
    label: string,
    isRequired?: boolean,
    isLabelHidden: boolean,

    isInvalid?: boolean,
    invalidMessage?: Node,

    children: Node
|};

export class FormField extends React.PureComponent<Props> {
    static defaultProps = {
        isLabelHidden: false
    };

    render() {
        const {children, isInvalid, invalidMessage, ...etc} = this.props;

        return (
            <Field
                {...etc}

                isInvalid={isInvalid || undefined}
                //$FlowFixMe
                invalidMessage={invalidMessage}

                validateOnChange={false}
                validateOnBlur={false}
            >
                {children}
            </Field>
        );
    }
}
