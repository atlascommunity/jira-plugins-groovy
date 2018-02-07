import React from 'react';
import PropTypes from 'prop-types';

import debounce from 'lodash.debounce';

import {FieldTextAreaStateless} from '@atlaskit/field-text-area';
import Spinner from '@atlaskit/spinner';
import {colors} from '@atlaskit/theme';

import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';

import {jiraService} from '../../service/services';


export class JqlInput extends React.Component {
    static propTypes = {
        label: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired,
        isRequired: PropTypes.bool,
        isInvalid: PropTypes.bool,
        invalidMessage: PropTypes.string
    };

    componentDidMount() {
        jiraService
            .getAutoCompleteData()
            .then(this._initAutoComplete);
    }

    state = {
        validating: false,
        isInvalid: null,
        invalidMessage: null,
        issues: null
    };

    _validate = (query) => {
        //todo: track requestId

        this.setState({
            validating: true
        });

        jiraService
            .validateQuery(query)
            .then(data => {
                this.setState({
                    validating: false,
                    isInvalid: false,
                    invalidMessage: null,
                    issues: data.total
                });
            })
            .catch(({response}) => {
                this.setState({
                    validating: false,
                    isInvalid: true,
                    invalidMessage: <div className="flex flex-column">
                        {response.data.errorMessages.map((message, i) => <div key={i}>{message}</div>)}
                    </div>,
                    issues: null
                });
            });
    };

    _debouncedValidate = debounce(this._validate, 500, { maxWait: 5000 });

    constructor(props) {
        super(props);

        this._debouncedValidate(props.value);
    }

    componentWillReceiveProps(props) {
        if (props.value !== this.props.value) {
            this._debouncedValidate(props.value);
        }
    }

    render() {
        const {value, onChange, isInvalid, invalidMessage, ...props} = this.props;
        const {issues, validating} = this.state;

        let invalid = isInvalid;
        let invalidMsg = invalidMessage;

        if (this.state.isInvalid !== null) {
            invalid = this.state.isInvalid;
            invalidMsg = this.state.invalidMessage;
        }

        return (
            <div>
                <FieldTextAreaStateless
                    {...props}

                    isInvalid={invalid}
                    invalidMessage={invalidMsg}

                    value={value}
                    onChange={onChange}
                />
                {validating &&
                    <div className="ak-description">
                        <Spinner size="small"/>
                        {' '}
                        Validating
                    </div>
                }
                {issues !== null &&
                    <div className="ak-description">
                        <CheckCircleIcon size="small" label="" primaryColor={colors.G500}/>
                        {' '}{issues} issues found
                    </div>
                }
            </div>
        );
    }
}
