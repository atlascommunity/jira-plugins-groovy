//@flow
import React, {type Node} from 'react';

import debounce from 'lodash/debounce';

import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';
import {colors} from '@atlaskit/theme';

import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';

import {FormField} from './FormField';

import {jiraService} from '../../service/services';
import {CommonMessages} from '../../i18n/common.i18n';

import type {FieldProps, ErrorType, MutableTextFieldProps} from '../types';
import type {ValidationResult} from '../../service/jira.service';


type JqlInputProps = FieldProps & MutableTextFieldProps<string, HTMLTextAreaElement>;

type JqlInputState = {
    validating: boolean,
    isInvalid: ?boolean,
    invalidMessage: ?Node,
    issues: ?number
};

export class JqlInput extends React.Component<JqlInputProps, JqlInputState> {
    state = {
        validating: false,
        isInvalid: null,
        invalidMessage: null,
        issues: null
    };

    _validate = (query: string) => {
        //todo: track requestId

        this.setState({
            validating: true
        });

        jiraService
            .validateQuery(query)
            .then((data: ValidationResult) => {
                this.setState({
                    validating: false,
                    isInvalid: false,
                    invalidMessage: null,
                    issues: data.total
                });
            })
            .catch(({response}: ErrorType) => {
                if (response.data) {
                    this.setState({
                        validating: false,
                        isInvalid: true,
                        invalidMessage: (
                            <div className="flex flex-column">
                                {response.data.errorMessages.map((message, i) =>
                                    <div key={i}>{message}</div>)
                                }
                            </div>
                        ),
                        issues: null
                    });
                }
            });
    };

    _debouncedValidate = debounce(this._validate, 500, { maxWait: 5000 });

    constructor(props: JqlInputProps) {
        super(props);

        this._debouncedValidate(props.value);
    }

    componentWillReceiveProps(props: JqlInputProps) {
        if (props.value !== this.props.value) {
            this._debouncedValidate(props.value);
        }
    }

    render() {
        const {label, isRequired, value, onChange, isInvalid, invalidMessage, ...props} = this.props;
        const {issues, validating} = this.state;

        let invalid: boolean = isInvalid || false;
        let invalidMsg: ?Node = invalidMessage;

        if (this.state.isInvalid) {
            invalid = this.state.isInvalid;
            invalidMsg = this.state.invalidMessage;
        }

        return (
            <div>
                <FormField
                    label={label || ''}
                    isRequired={isRequired}

                    isInvalid={invalid}
                    invalidMessage={invalidMsg}
                >
                    <FieldTextAreaStateless
                        {...props}

                        value={value}
                        onChange={onChange}
                    />
                </FormField>
                {validating &&
                    <div className="ak-description">
                        <Spinner size="small"/>
                        {' '}{CommonMessages.validating}
                    </div>
                }
                {!!issues &&
                    <div className="ak-description">
                        <CheckCircleIcon size="small" label="" primaryColor={colors.G500}/>
                        {' '}{CommonMessages.issuesFound(issues.toString())}
                    </div>
                }
            </div>
        );
    }
}
