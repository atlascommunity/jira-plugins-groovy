import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import Button from 'aui-react/lib/AUIButton';
import Modal from 'aui-react/lib/AUIDialog';
import Message from 'aui-react/lib/AUIMessage';

import {ScriptActionCreators} from './rest.reducer';

import {AUIRequired} from '../common/aui-components';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {restService} from '../service/services';
import {Editor} from '../common/Editor';
import {MultiSelect2} from '../common/MultiSelect2';
import {getMarkers} from '../common/error';


const httpMethods = ['GET', 'POST', 'PUT', 'DELETE'].map(method => { return { label: method, value: method }; });

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
                            methods: script.methods.map(method => { return { label: method, value: method }; }),
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

        const jsData = this.state.values.toJS();
        const data = {
            ...jsData,
            methods: jsData.methods ? jsData.methods.map(option => option.value) : null
        };

        //todo: validation & error display
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
            let annotations = null;

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
                <form className="aui" onSubmit={this._onSubmit}>
                    {error && !errorField ?
                        <Message type="error">
                            {errorMessage}
                        </Message>
                    : null}

                    <div className="field-group">
                        <label htmlFor="rest-script-dialog-name">
                            {FieldMessages.name}
                            <AUIRequired/>
                        </label>
                        <input
                            type="text"
                            className="text long-field"
                            id="rest-script-dialog-name"
                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                        <div className="description">{RestMessages.nameDescription}</div>
                        {errorField === 'name' && <div className="error">{errorMessage}</div>}
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.httpMethods}
                        </label>
                        <MultiSelect2
                            className="long-field"

                            options={httpMethods}
                            value={values.get('methods')}
                            onChange={this._setObjectValue('methods')}
                        />
                        {errorField === 'methods' && <div className="error">{errorMessage}</div>}
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.scriptCode}
                            <AUIRequired/>
                        </label>
                        <Editor
                            mode="groovy"

                            onChange={this._setObjectValue('scriptBody')}
                            value={values.get('scriptBody') || ''}

                            markers={markers}
                            annotations={annotations}
                        />
                        {errorField === 'scriptBody' && <div className="error">{errorMessage}</div>}
                    </div>
                    {!isNew && <div className="field-group">
                        <label htmlFor="rest-script-dialog-comment">
                            {FieldMessages.comment}
                            <AUIRequired/>
                        </label>
                        <textarea
                            id="rest-script-dialog-comment"
                            className="textarea long-field"

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                        {errorField === 'comment' && <div className="error">{errorMessage}</div>}
                    </div> }
                </form>;
        }

        return <Modal
            size="xlarge"
            titleContent={`${isNew ? RestMessages.createScript : RestMessages.updateScript}`}
            onClose={onClose}
            footerActionContent={[
                <Button key="create" onClick={this._onSubmit}>
                    {isNew ? CommonMessages.create : CommonMessages.update}
                </Button>,
                <Button key="close" type="link" onClick={onClose}>{CommonMessages.cancel}</Button>
            ]}
            type="modal"
            styles={{zIndex: '3000'}}
        >
            {body}
        </Modal>;
    }
}
