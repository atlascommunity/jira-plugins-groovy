//@flow
import React, {type Node} from 'react';

import Base, {Label} from '@atlaskit/field-base';

import type {BindingType, MarkerType, ReturnType} from '../editor/types';
import type {FieldProps, MutableFieldProps, AkFormFieldProps} from '../types';

import Editor, {EditorThemeContextConsumer} from '../editor';

import type {CodeMirrorType, ValidationState, LinterType} from '../editor';


import './EditorField.less';


type EditorFieldProps = {|
    ...FieldProps,
    ...MutableFieldProps<string>,
    ...AkFormFieldProps,
    mode: string,
    markers?: $ReadOnlyArray<MarkerType>,
    bindings?: $ReadOnlyArray<BindingType>,
    returnTypes?: $ReadOnlyArray<ReturnType>,
    resizable?: boolean,
    validationState?: ValidationState,
    editorDidMount?: (editor: CodeMirrorType) => void,
    linter?: LinterType,
|};

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
        const {value, label, isRequired, isLabelHidden, mode, shouldFitContainer, isInvalid, invalidMessage, isValidationHidden, ...props} = this.props;

        return (
            <div className="ak-editor">
                <Label label={label || ''} isRequired={isRequired} isLabelHidden={isLabelHidden}/>
                <EditorThemeContextConsumer>
                    {context =>
                        <Editor
                            decorator={this._decorateEditor}
                            mode={mode}
                            value={value || ''}
                            {...context}
                            {...props}
                        />
                    }
                </EditorThemeContextConsumer>
            </div>
        );
    }
}
