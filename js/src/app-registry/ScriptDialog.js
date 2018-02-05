import React from 'react';

import {connect} from 'react-redux';

import Dialog from 'aui-react/lib/AUIDialog';
import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

import Spinner from '@atlaskit/spinner';

import {Map} from 'immutable';

import {RegistryActionCreators} from './registry.reducer';

import {FieldMessages, CommonMessages} from '../i18n/common.i18n';

import {registryService} from '../service/services';
import {Editor} from '../common/Editor';

import {getMarkers} from '../common/error';
import {StaticField} from '../common/StaticField';
import {Bindings} from '../common/bindings';


@connect(null, RegistryActionCreators, null, {withRef: true})
export class ScriptDialog extends React.Component {
    state = {
        active: false,
        id: null,
        values: new Map(),
        parentName: '',
        error: null,
        modified: false,
        waiting: false
    };

    activateCreate = (directoryId) => {
        registryService
            .getDirectory(directoryId)
            .then(directory =>
                this.setState({
                    active: true,
                    id: null,
                    values: new Map({
                        directoryId: directoryId
                    }),
                    parentName: directory.fullName,
                    error: null,
                    waiting: false
                })
            );
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
                parentName: data.parentName,
                error: null,
                waiting: false
            }));
        //todo: show something when loading
    };

    _handleError = (error) => {
        const {response} = error;

        this.setState({ waiting: false });
        if (response.status === 400) {
            this.setState({
                error: response.data,
                modified: false
            });
        } else {
            throw error;
        }
    };

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        this.setState({ waiting: true });

        const id = this.state.id;
        if (id) {
            registryService
                .updateScript(id, this.state.values.toJS())
                .then(
                    data => {
                        this.props.updateScript(data);
                        this.setState({ active: false, waiting: false });
                    },
                    this._handleError
                );
        } else {
            registryService
                .createScript(this.state.values.toJS())
                .then(
                    data => {
                        this.props.addScript(data);
                        this.setState({ active: false, waiting: false });
                    },
                    this._handleError
                );
        }
    };

    _close = () => this.setState({ active: false, waiting: false });

    mutateValue = (field, value) => {
        this.setState(state => {
            return {
                values: state.values.set(field, value),
                modified: true
            };
        });
    };

    _setTextValue = (field) => (event) => this.mutateValue(field, event.target.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    render() {
        const {values, parentName, error, modified, waiting} = this.state;
        let errorMessage = null;
        let errorField = null;

        let markers = null;
        if (error) {
            if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                const errors = error.error.filter(e => e);
                if (!modified) {
                    markers = getMarkers(errors);
                }
                errorMessage = errors
                    .map(error => error.message)
                    .map(error => <p key={error}>{error}</p>);
            } else {
                errorMessage = error.message;
            }
            errorField = error.field;
        }

        console.log(error, markers);

        return (
            <div>
                {this.state.active ?
                    <Dialog
                        size="xlarge"
                        titleContent={`${this.state.id ? CommonMessages.update : CommonMessages.create} script`}
                        onClose={this._close}
                        footerActionContent={[
                            <Button
                                key="create"
                                onClick={this._onSubmit}
                                disabled={waiting}
                            >
                                {waiting && <Spinner size={15}/>}
                                {!waiting && (this.state.id ? CommonMessages.update : CommonMessages.create)}
                            </Button>,
                            <Button
                                key="close"
                                type="link"
                                onClick={this._close}
                                disabled={waiting}
                            >
                                {CommonMessages.cancel}
                            </Button>
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
                                    {FieldMessages.parentName}
                                </label>
                                <StaticField>
                                    {parentName}
                                </StaticField>
                            </div>
                            <div className="field-group">
                                <label htmlFor="directory-dialog-name">
                                    {FieldMessages.name}
                                    <span className="aui-icon icon-required"/>
                                </label>
                                <input
                                    type="text"
                                    className="text full-width-field"
                                    id="directory-dialog-name"
                                    value={values.get('name') || ''}
                                    onChange={this._setTextValue('name')}
                                    disabled={waiting}
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
                                    decorated={true}
                                    bindings={[
                                        Bindings.mutableIssue, Bindings.currentUser, Bindings.transientVars
                                    ]}

                                    onChange={this._setObjectValue('scriptBody')}
                                    value={values.get('scriptBody') || ''}
                                    isDisabled={waiting}

                                    markers={markers}
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

                                    className="textarea full-width-field"
                                    value={values.get('comment')}
                                    onChange={this._setTextValue('comment')}
                                    disabled={waiting}
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
