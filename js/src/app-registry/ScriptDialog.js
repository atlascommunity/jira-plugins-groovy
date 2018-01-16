import React from 'react';

import {connect} from 'react-redux';

import Dialog from 'aui-react/lib/AUIDialog';
import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

import {Map} from 'immutable';

import {RegistryActionCreators} from './registry.reducer';

import {FieldMessages, CommonMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';
import {Editor} from '../common/Editor';

import {getMarkers} from '../common/error';


@connect(null, RegistryActionCreators, null, {withRef: true})
export class ScriptDialog extends React.Component {
    state = {
        active: false,
        id: null,
        values: new Map(),
        error: null
    };

    activateCreate = (directoryId) => {
        this.setState({
            active: true,
            id: null,
            values: new Map({
                directoryId: directoryId
            }),
            error: null
        });
    };

    activateEdit = (id) => {
        registryService
            .getScript(id)
            .then(data => this.setState({
                active: true,
                id: id,
                values: new Map({
                    name: data.name,
                    scriptBody: data.scriptBody,
                    directoryId: data.directoryId
                }),
                error: null
            }));
        //todo: show something when loading
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

        const id = this.state.id;
        if (id) {
            registryService
                .updateScript(id, this.state.values.toJS())
                .then(
                    data => {
                        this.props.updateScript(data);
                        this.setState({active: false});
                    },
                    this._handleError
                );
        } else {
            registryService
                .createScript(this.state.values.toJS())
                .then(
                    data => {
                        this.props.addScript(data);
                        this.setState({active: false});
                    },
                    this._handleError
                );
        }
    };

    _close = () => this.setState({active: false});

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
        const {values, error} = this.state;
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

        console.log(error, markers, annotations);

        return (
            <div>
                {this.state.active ?
                    <Dialog
                        size="xlarge"
                        titleContent={`${this.state.id ? CommonMessages.update : CommonMessages.create} script`}
                        onClose={this._close}
                        footerActionContent={[
                            <Button key="create" onClick={this._onSubmit}>
                                {this.state.id ? CommonMessages.update : CommonMessages.create}
                            </Button>,
                            <Button key="close" type="link" onClick={this._close}>{CommonMessages.cancel}</Button>
                        ]}
                        type="modal"
                        styles={{zIndex: '3000'}}
                    >
                        {error && !errorField ?
                            <Message type="error">
                                {errorMessage}
                            </Message>
                        : null}
                        <form className="aui" onSubmit={this._onSubmit}>
                            <div className="field-group">
                                <label htmlFor="directory-dialog-name">
                                    {FieldMessages.name}
                                    <span className="aui-icon icon-required"/>
                                </label>
                                <input
                                    type="text"
                                    className="text long-field"
                                    id="directory-dialog-name"
                                    value={values.get('name') || ''}
                                    onChange={this._setTextValue('name')}
                                />
                                {errorField === 'name' && <div className="error">{errorMessage}</div>}
                            </div>
                            <div className="field-group">
                                <label>
                                    {FieldMessages.scriptCode}
                                    <span className="aui-icon icon-required"/>
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
                            {this.state.id ? <div className="field-group">
                                <label htmlFor="directory-dialog-comment">
                                    {FieldMessages.comment}
                                    <span className="aui-icon icon-required"/>
                                </label>
                                <textarea
                                    id="directory-dialog-comment"

                                    className="textarea long-field"
                                    value={values.get('comment')}
                                    onChange={this._setTextValue('comment')}
                                />
                                {errorField === 'comment' && <div className="error">{errorMessage}</div>}
                            </div> : null}
                        </form>
                    </Dialog>
                    : null}
            </div>
        );
    }
}
