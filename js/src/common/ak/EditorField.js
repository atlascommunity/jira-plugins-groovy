import React from 'react';
import PropTypes from 'prop-types';

import Base, {Label} from '@atlaskit/field-base';

import {Editor, MarkerShape, BindingShape} from '../editor/Editor';

import './EditorField.less';


export class EditorField extends React.Component {
    static propTypes = {
        label: PropTypes.string.isRequired,
        mode: PropTypes.string.isRequired,
        isLabelHidden: PropTypes.bool,

        value: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired,

        markers: PropTypes.arrayOf(MarkerShape.isRequired),
        bindings: PropTypes.arrayOf(BindingShape.isRequired),
        resizable: PropTypes.bool,

        isInvalid: PropTypes.bool,
        invalidMessage: PropTypes.string,

        isRequired: PropTypes.bool,
        isDisabled: PropTypes.bool
    };

    static defaultProps = {
        mode: 'groovy'
    };

    _decorateEditor = (editor) => {
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
