//@flow
import React from 'react';

import {Bindings} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {CommonMessages} from '../i18n/common.i18n';


const bindings = [ Bindings.mutableIssue, Bindings.currentUser, Bindings.transientVars ];

type FormEditorProps = {
    initialValue: ?string,
    fieldName: string
};

type FormEditorState = {
    value: string
};

export class FormEditor extends React.Component<FormEditorProps, FormEditorState> {
    state = {
        value: this.props.initialValue || ''
    };

    _setValue = (value: ?string) => {
        this.setState({ value: value || '' });
    };

    render() {
        return <div>
            <EditorField
                label={CommonMessages.script}
                isRequired={true}

                bindings={bindings}

                value={this.state.value}
                onChange={this._setValue}
            />
            <textarea
                className="hidden"

                readOnly={true}
                value={this.state.value}
                name={this.props.fieldName}
            />
        </div>;
    }
}
