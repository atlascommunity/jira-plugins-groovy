//@flow
import * as React from 'react';

import Base, {Label} from '@atlaskit/field-base';

import type {BindingType, MarkerType} from '../editor/types';

import Editor from '../editor';

import './EditorField.less';


type EditorFieldProps = {
    label: string,
    mode: string,
    isLabelHidden?: boolean,

    value: string,
    onChange: (string) => void,

    markers?: Array<MarkerType>,
    bindings?: Array<BindingType>,
    resizable?: boolean,

    isInvalid?: boolean,
    invalidMessage?: string,

    isRequired?: boolean,
    isDisabled?: boolean
}

export class EditorField extends React.Component<EditorFieldProps> {
    static defaultProps = {
        mode: 'groovy'
    };

    _decorateEditor = (editor : React.Node) : React.Node => {
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

    render() {
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
