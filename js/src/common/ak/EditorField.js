import React from 'react';
import PropTypes from 'prop-types';

import Base, {Label} from '@atlaskit/field-base';

import {Editor, MarkerShape, BindingShape} from '../Editor';

import './EditorField.less';


export class EditorField extends React.Component {
    static propTypes = {
        label: PropTypes.string.isRequired,
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
        const {label, isRequired, isLabelHidden, isDisabled, markers, resizable, bindings, value, onChange} = this.props;

        return (
            <div className="ak-editor">
                <Label label={label} isRequired={isRequired} isLabelHidden={isLabelHidden}/>
                <Editor
                    mode="groovy"
                    decorator={this._decorateEditor}

                    isDisabled={isDisabled}
                    markers={markers}

                    bindings={bindings}
                    resizable={resizable}

                    onChange={onChange}
                    value={value}
                />
            </div>
        );
    }
}
