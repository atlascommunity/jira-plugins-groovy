import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {restService} from '../service/services';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';
import {MultiSelect} from '../common/ak/MultiSelect';
import {EditorField} from '../common/ak/EditorField';
import {AsyncPicker} from '../common/ak/AsyncPicker';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {ErrorMessage} from '../common/ak/messages';
import {RegistryMessages} from '../i18n/registry.i18n';
import {ItemActionCreators} from '../common/redux';


const httpMethods = ['GET', 'POST', 'PUT', 'DELETE'].map(method => { return { label: method, value: method }; });
const bindings = [ Bindings.method, Bindings.headers, Bindings.uriInfo, Bindings.body, Bindings.currentUser ];

@connect(
    null,
    ItemActionCreators
)
export class RestScriptDialog extends React.Component {
    static propTypes = {
        isNew: PropTypes.bool.isRequired,
        onClose: PropTypes.func.isRequired,
        id: PropTypes.number,
        updateItem: PropTypes.func.isRequired,
        addItem: PropTypes.func.isRequired
    };

    state = {
        ready: false,
        values: null,
        error: null,
        script: null
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
                    groups: [],
                    scriptBody: ''
                }),
                error: null,
                script: null
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
                            groups: script.groups.map(group => {
                                return {
                                    label: group,
                                    value: group
                                };
                            }),
                            description: script.description,
                            comment: ''
                        }),
                        ready: true,
                        script
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

        const {groups, ...data} = this.state.values.toJS();
        data.groups = groups ? groups.map(group => group.value) : [];

        if (isNew) {
            restService
                .createScript(data)
                .then(
                    script => {
                        onClose();
                        this.props.addItem(script);
                    },
                    this._handleError
                );
        } else {
            restService
                .updateScript(id, data)
                .then(
                    script => {
                        onClose();
                        this.props.updateItem(script);
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
        const {ready, values, script, error} = this.state;

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
                    {error && !errorField && <ErrorMessage title={errorMessage}/>}

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

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        minimumRows={5}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorField === 'description' ? errorMessage : null}

                        label={FieldMessages.description}
                        value={values.get('description') || ''}
                        onChange={this._setTextValue('description')}
                    />

                    <MultiSelect
                        label={FieldMessages.httpMethods}
                        isRequired={true}

                        isInvalid={errorField === 'methods'}
                        invalidMessage={errorField === 'methods' ? <div className="error">{errorMessage}</div> : ''}

                        items={httpMethods}
                        value={values.get('methods')}
                        onChange={this._setObjectValue('methods')}
                    />
                    <AsyncPicker
                        label={FieldMessages.groups}
                        isMulti={true}

                        src={`${getPluginBaseUrl()}/jira-api/groupPicker`}

                        value={values.get('groups')}
                        onChange={this._setObjectValue('groups')}

                        isInvalid={errorField === 'groups'}
                        invalidMessage={errorField === 'groups' ? errorMessage : ''}
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
                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        required={!isNew}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorField === 'comment' ? errorMessage : null}

                        label={FieldMessages.comment}
                        value={values.get('comment') || ''}
                        onChange={this._setTextValue('comment')}
                    />
                </div>;
        }

        return <ModalDialog
            width="x-large"
            scrollBehavior="outside"

            isHeadingMultiline={false}
            heading={isNew ? RegistryMessages.addScript : `${RegistryMessages.editScript}: ${script && script.name}`}

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
