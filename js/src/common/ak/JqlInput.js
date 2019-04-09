//@flow
import React, {type Node} from 'react';

import debounce from 'lodash/debounce';

import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';
import {colors} from '@atlaskit/theme';

import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';

import {FormField} from './FormField';

import {jiraService} from '../../service';
import {CommonMessages} from '../../i18n/common.i18n';

import type {FieldProps, ErrorType, MutableTextFieldProps} from '../types';
import type {JqlQueryValidationResult} from '../../service';


type Props = {|
    ...FieldProps,
    ...MutableTextFieldProps<string, HTMLTextAreaElement>,
    shouldFitContainer?: boolean
|};

type State = {
    validating: boolean,
    isInvalid: ?boolean,
    invalidMessage: ?Node,
    issues: ?number
};

export class JqlInput extends React.Component<Props, State> {
    state = {
        validating: false,
        isInvalid: null,
        invalidMessage: null,
        issues: null
    };

    _validate = (query: string) => {
        this.setState({
            validating: true
        });

        jiraService
            .validateQuery(query)
            .then((data: JqlQueryValidationResult) => {
                this.setState({
                    validating: false,
                    isInvalid: false,
                    invalidMessage: null,
                    issues: data.total
                });
            })
            .catch(({response}: ErrorType) => {
                const data = response.data;
                if (data) {
                    this.setState(
                        (_state, {value}: Props): $Shape<State> => {
                            if (query === value) {
                                return {
                                    validating: false,
                                    isInvalid: true,
                                    invalidMessage: (
                                        <div className="flex flex-column">
                                            {data.errorMessages.map((message, i) => <div key={i}>{message}</div>)}
                                        </div>
                                    ),
                                    issues: null
                                };
                            }

                            return {};
                        });
                }
            });
    };

    _debouncedValidate = debounce(this._validate, 500, { maxWait: 5000 });

    constructor(props: Props) {
        super(props);

        this._debouncedValidate(props.value);
    }

    componentDidUpdate(prevProps: Props) {
        const {value} = this.props;

        if (prevProps.value !== value) {
            this._debouncedValidate(value);
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
