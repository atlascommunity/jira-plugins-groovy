import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import Message from 'aui-react/lib/AUIMessage';

import {ScriptActionCreators} from './rest.reducer';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {restService} from '../service/services';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {MultiSelect} from '../common/ak/MultiSelect';
import {EditorField} from '../common/ak/EditorField';


const httpMethods = ['GET', 'POST', 'PUT', 'DELETE'].map(method => { return { content: method, value: method }; });
const bindings = [ Bindings.method, Bindings.uriInfo, Bindings.body, Bindings.currentUser ];

@connect(
    () => { return{}; },
    ScriptActionCreators
)
export class RestScriptDialog extends React.Component {
    static propTypes = {
        isNew: PropTypes.bool.isRequired,
        onClose: PropTypes.func.isRequired,
        id: PropTypes.number,
        updateScript: PropTypes.func.isRequired,
        addScript: PropTypes.func.isRequired
    };

    state = {
        ready: false,
        values: null
    };

    componentWillReceiveProps(nextProps) {
        this._init(nextProps);
    }

    componentDidMount() {
        this._init(this.props);
    }

    _init = props => {
        if (props.isNew) {
            this.setState({
                ready: true,
                values: new Map({
                    name: '',
                    methods: [],
                    scriptBody: ''
                }),
                error: null
            });
        } else {
            this.setState({
                ready: false,
                values: null,
                error: null
            });

            restService
                .getScript(props.id)
                .then(script => {
                    this.setState({
                        values: new Map({
                            name: script.name,
                            methods: script.methods,
                            scriptBody: script.scriptBody,
                            comment: ''
                        }),
                        ready: true
                    });
                });
        }
    };

    _handleError = (error) => {
        const {response} = error;

        if (response.status === 400) {
            this.setState({error: response.data});
        } else {
            throw error;
        }
    };

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        const {isNew, id, onClose} = this.props;

        const data = this.state.values.toJS();

        if (isNew) {
            restService
                .createScript(data)
                .then(
                    script => {
                        onClose();
                        this.props.addScript(script);
                    },
                    this._handleError
                );
        } else {
            restService
                .updateScript(id, data)
                .then(
                    script => {
                        onClose();
                        this.props.updateScript(script);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field, value) => {
        this.setState(state => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setTextValue = (field) => (event) => this.mutateValue(field, event.target.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    render() {
        const {onClose, isNew} = this.props;
        const {ready, values, error} = this.state;

        let body = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            let errorMessage = null;
            let errorField = null;

            let markers = null;

            if (error) {
                if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                    const errors = error.error.filter(e => e);
                    markers = getMarkers(errors);
                    errorMessage = errors
                        .map(error => error.message)
                        .map(error => <p key={error}>{error}</p>);
                } else {
                    errorMessage = error.message;
                }
                errorField = error.field;
            }

            body =
                <div className="flex-column">
                    {error && !errorField ?
                        <Message type="error">
                            {errorMessage}
                        </Message>
                    : null}

                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={values.get('name') || ''}
                        onChange={this._setTextValue('name')}
                    />
                    <div className="ak-description">{RestMessages.nameDescription}</div>

                    <MultiSelect
                        label={FieldMessages.httpMethods}

                        isInvalid={errorField === 'methods'}
                        invalidMessage={errorField === 'methods' ? <div className="error">{errorMessage}</div> : ''}

                        items={httpMethods}
                        value={values.get('methods')}
                        onChange={this._setObjectValue('methods')}
                    />
                    <EditorField
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                        markers={markers}

                        bindings={bindings}

                        value={values.get('scriptBody') || ''}
                        onChange={this._setObjectValue('scriptBody')}
                    />
                    {!isNew &&
                        <FieldTextAreaStateless
                            shouldFitContainer={true}
                            required={true}

                            isInvalid={errorField === 'comment'}
                            invalidMessage={errorField === 'comment' ? errorMessage : null}

                            label={FieldMessages.comment}
                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    }
                </div>;
        }

        return <ModalDialog
            width="x-large"
            scrollBehavior="outside"
            heading={`${isNew ? RestMessages.createScript : RestMessages.updateScript}`}
            onClose={onClose}
            actions={[
                {
                    text: isNew ? CommonMessages.create : CommonMessages.update,
                    onClick: this._onSubmit,
                },
                {
                    text: CommonMessages.cancel,
                    onClick: onClose,
                }
            ]}
        >
            {body}
        </ModalDialog>;
    }
}
