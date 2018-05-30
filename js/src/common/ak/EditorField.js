//@flow
import React, {type Node} from 'react';

import Base, {Label} from '@atlaskit/field-base';

import type {BindingType, MarkerType, ReturnType} from '../editor/types';
import type {FieldProps, MutableFieldProps, AkFormFieldProps} from '../types';

import Editor from '../editor';

import './EditorField.less';


type EditorFieldProps = FieldProps & MutableFieldProps<string> & AkFormFieldProps & {
    mode: string,
    markers?: ?$ReadOnlyArray<MarkerType>,
    bindings?: $ReadOnlyArray<BindingType>,
    returnTypes?: $ReadOnlyArray<ReturnType>,
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

    render() {
        const {label, isRequired, isLabelHidden, mode, ...props} = this.props;

        return (
            <div className="ak-editor">
                <Label label={label || ''} isRequired={isRequired} isLabelHidden={isLabelHidden}/>
                <Editor
                    decorator={this._decorateEditor}
                    mode={mode}
                    {...props}
                />
            </div>
        );
    }
}
