import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import Button from 'aui-react/lib/AUIButton';
import Modal from 'aui-react/lib/AUIDialog';
import Message from 'aui-react/lib/AUIMessage';

import {ConditionPicker} from './ConditionPicker';
import {ListenerActionCreators} from './listeners.reducer';

import {AUIRequired} from '../common/aui-components';

import {ListenerMessages} from '../i18n/listener.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {fillListenerKeys} from '../model/listener.model';

import {listenerService} from '../service/services';
import {Editor} from '../common/Editor';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';


//AbstractProjectEvent
//ProjectCategoryChangeEvent
//AbstractVersionEvent
//AbstractProjectComponentEvent
//DirectoryEvent
//AbstractCustomFieldEvent
//AbstractWorklogEvent
//IndexEvent
//AbstractRemoteIssueLinkEvent
//IssueWatcherAddedEvent
//IssueWatcherDeletedEvent

function extractShortClassName(className) {
    if (className.indexOf('.') !== -1) {
        const tokens = className.split('.');
        return tokens[tokens.length-1];
    }
}

@connect(
    () => { return{}; },
    ListenerActionCreators
)
export class ListenerDialog extends React.Component {
    static propTypes = {
        isNew: PropTypes.bool.isRequired,
        onClose: PropTypes.func.isRequired,
        id: PropTypes.number,
        updateListener: PropTypes.func.isRequired,
        addListener: PropTypes.func.isRequired
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
                    condition: {},
                    name: '',
                    comment: '',
                    scriptBody: ''
                })
            });
        } else {
            this.setState({
                ready: false,
                values: null
            });

            listenerService
                .getListener(props.id)
                .then(rawListener => {
                    const listener = fillListenerKeys(rawListener);
                    this.setState({
                        values: new Map({
                            name: listener.name,
                            scriptBody: listener.scriptBody,
                            condition: listener.condition
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
            listenerService
                .createListener(data)
                .then(
                    listener => {
                        onClose();
                        this.props.addListener(fillListenerKeys(listener));
                    },
                    this._handleError
                );
        } else {
            listenerService
                .updateListener(id, data)
                .then(
                    listener => {
                        onClose();
                        this.props.updateListener(fillListenerKeys(listener));
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

            let bindings = null;

            const condition = values.get('condition');
            if (condition && condition.type) {
                if (condition.type === 'ISSUE') {
                    bindings = [Bindings.issueEvent];
                } else {
                    const className = condition.className;
                    if (className) {
                        const extractedName = extractShortClassName(className);
                        if (extractedName) {
                            bindings = [{
                                ...Bindings.event,
                                className: extractedName,
                                fullClassName: className
                            }];
                        }
                    }
                }
            }

            body =
                <form className="aui" onSubmit={this._onSubmit}>
                    {error && !errorField ?
                        <Message type="error">
                            {errorMessage}
                        </Message>
                    : null}

                    <div className="field-group">
                        <label htmlFor="listener-dialog-name">
                            {FieldMessages.name}
                            <AUIRequired/>
                        </label>
                        <input
                            type="text"
                            className="text full-width-field"
                            id="listener-dialog-name"
                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                        {errorField === 'name' && <div className="error">{errorMessage}</div>}
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.condition}
                            <AUIRequired/>
                        </label>
                        <ConditionPicker value={condition} onChange={this._setObjectValue('condition')}/>
                        {errorField === 'condition' && <div className="error">{errorMessage}</div>}
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.scriptCode}
                            <AUIRequired/>
                        </label>
                        <Editor
                            mode="groovy"
                            decorated={true}
                            bindings={bindings}

                            onChange={this._setObjectValue('scriptBody')}
                            value={values.get('scriptBody') || ''}

                            markers={markers}
                            annotations={annotations}
                        />
                        {errorField === 'scriptBody' && <div className="error">{errorMessage}</div>}
                    </div>
                    {!isNew && <div className="field-group">
                        <label htmlFor="listener-dialog-comment">
                            {FieldMessages.comment}
                            <AUIRequired/>
                        </label>
                        <textarea
                            id="listener-dialog-comment"
                            className="textarea full-width-field"

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                        {errorField === 'comment' && <div className="error">{errorMessage}</div>}
                    </div> }
                </form>;
        }

        return <Modal
            size="xlarge"
            titleContent={`${isNew ? ListenerMessages.createListener : ListenerMessages.updateListener}`}
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
