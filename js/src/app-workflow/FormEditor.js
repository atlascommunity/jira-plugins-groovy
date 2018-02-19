import React from 'react';
import PropTypes from 'prop-types';

import {Bindings} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {CommonMessages} from '../i18n/common.i18n';


const bindings = [ Bindings.mutableIssue, Bindings.currentUser, Bindings.transientVars ];

export class FormEditor extends React.Component {
    static propTypes = {
        initialValue: PropTypes.string,
        fieldName: PropTypes.string.isRequired
    };

    state = {
        value: this.props.initialValue
    };

    _setValue = (value) => this.setState({ value });

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
