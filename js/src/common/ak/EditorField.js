//@flow
import React, {type Node} from 'react';

import Base, {Label} from '@atlaskit/field-base';

import type {BindingType, MarkerType} from '../editor/types';
import type {FieldProps, MutableFieldProps, AkFormFieldProps} from '../types';

import Editor from '../editor';

import './EditorField.less';


type EditorFieldProps = FieldProps & MutableFieldProps<string> & AkFormFieldProps & {
    mode: string,
    markers?: ?Array<MarkerType>,
    bindings?: Array<BindingType>,
    resizable?: boolean,
};

export class EditorField extends React.Component<EditorFieldProps> {
    static defaultProps = {
        mode: 'groovy',
        isValidationHidden: false
    };

    _decorateEditor = (editor: Node): Node => {
        const {isInvalid, invalidMessage, isValidationHidden, isRequired, isDisabled} = this.props;

        return (
            <Base
                invalidMessage={invalidMessage}
                isPaddingDisabled={true}
                isDisabled={isDisabled}
                isFitContainerWidthEnabled={true}
                isValidationHidden={isValidationHidden}
                isInvalid={isInvalid}
                isRequired={isRequired}
            >
                {editor}
            </Base>
        );
    };

    render(): Node {
        const {label, isRequired, isLabelHidden, ...props} = this.props;

        return (
            <div className="ak-editor">
                <Label label={label} isRequired={isRequired} isLabelHidden={isLabelHidden}/>
                <Editor
                    decorator={this._decorateEditor}
                    {...props}
                />
            </div>
        );
    }
}
