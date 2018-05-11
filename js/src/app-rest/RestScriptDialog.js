//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';
import type {Map as MapType} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import type {RestScriptType} from './types';

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
import type {FullDialogComponentProps} from '../common/script-list/types';
import type {HttpMethod} from '../common/types';
import type {OldSelectItem} from '../common/ak/types';
import type {InputEvent} from '../common/EventTypes';


const httpMethods = ['GET', 'POST', 'PUT', 'DELETE'].map(
    (method: HttpMethod): OldSelectItem<string> => {
        return {
            label: method,
            value: method
        };
    }
);

const bindings = [ Bindings.method, Bindings.headers, Bindings.uriInfo, Bindings.body, Bindings.currentUser ];

const {updateItem, addItem} = ItemActionCreators;

type Props = FullDialogComponentProps & {
    updateItem: typeof updateItem,
    addItem: typeof addItem
};

type State = {
    ready: boolean,
    values: MapType<string, any>,
    error: *,
    script: ?RestScriptType
};

export class RestScriptDialogInternal extends React.Component<Props, State> {
    static propTypes = {
        isNew: PropTypes.bool.isRequired,
        onClose: PropTypes.func.isRequired,
        id: PropTypes.number,
        updateItem: PropTypes.func.isRequired,
        addItem: PropTypes.func.isRequired
    };

    state = {
        ready: false,
        values: Map(),
        error: null,
        script: null
    };

    componentWillReceiveProps(nextProps: Props) {
        this._init(nextProps);
    }

    componentDidMount() {
        this._init(this.props);
    }

    _init = ({isNew, id}: Props) => {
        if (!isNew && id) {
            this.setState({
                ready: false,
                values: Map(),
                error: null
            });

            restService
                .getScript(id)
                .then((script: RestScriptType) => {
                    this.setState({
                        values: Map({
                            name: script.name,
                            methods: script.methods,
                            scriptBody: script.scriptBody,
                            groups: script.groups.map((group: string): OldSelectItem<string> => {
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
        } else {
            this.setState({
                ready: true,
                values: Map({
                    name: '',
                    methods: [],
                    groups: [],
                    scriptBody: ''
                }),
                error: null,
                script: null
            });
        }
    };

    _handleError = (error: *) => {
        const {response} = error;

        if (response.status === 400) {
            this.setState({error: response.data});
        } else {
            throw error;
        }
    };

    _onSubmit = () => {
        const {isNew, id, onClose} = this.props;

        const {groups, ...data}: any = this.state.values.toJS();
        data.groups = groups ? groups.map(group => group.value) : [];

        if (!isNew && id) {
            restService
                .updateScript(id, data)
                .then(
                    (script: RestScriptType) => {
                        onClose();
                        this.props.updateItem(script);
                    },
                    this._handleError
                );
        } else {
            restService
                .createScript(data)
                .then(
                    (script: RestScriptType) => {
                        onClose();
                        this.props.addItem(script);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: string, value: any) => {
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setTextValue = (field: string) => (event: InputEvent) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field: string) => (value: any) => this.mutateValue(field, value);

    render(): React.Node {
        const {onClose, isNew} = this.props;
        const {ready, values, script, error} = this.state;

        let body: ?React.Node = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            let errorMessage: * = null;
            let errorField: ?string = null;

            let markers: * = null;

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
            heading={isNew ? RegistryMessages.addScript : `${RegistryMessages.editScript}: ${script ? script.name : ''}`}

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

export const RestScriptDialog = connect(
    null,
    { addItem, updateItem }
)(RestScriptDialogInternal);
