//@flow
import * as React from 'react';

import Base, {Label} from '@atlaskit/field-base';

import type {BindingType, MarkerType} from '../editor/types';
import type {FieldProps, MutableFieldProps} from '../types';

import Editor from '../editor';

import './EditorField.less';


type EditorFieldProps = FieldProps & MutableFieldProps<string> & {
    mode: string,
    markers?: ?Array<MarkerType>,
    bindings?: Array<BindingType>,
    resizable?: boolean,
}

export class EditorField extends React.Component<EditorFieldProps> {
    static defaultProps = {
        mode: 'groovy'
    };

    _decorateEditor = (editor: Editor): React.Node => {
        const {isInvalid, invalidMessage, isRequired, isDisabled} = this.props;

        return (
            <Base
                invalidMessage={invalidMessage}
                isPaddingDisabled={true}
                isDisabled={isDisabled}
                isFitContainerWidthEnabled={true}
                isInvalid={isInvalid}
                isRequired={isRequired}
            >
                {editor}
            </Base>
        );
    };

    render(): React.Node {
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
