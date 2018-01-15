import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {Map} from 'immutable';

import Button from 'aui-react/lib/AUIButton';
import Modal from 'aui-react/lib/AUIDialog';

import {ScriptActionCreators} from './rest.reducer';

import {AUIRequired} from '../common/aui-components';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {restService} from '../service/services';
import {Editor} from '../common/Editor';


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
                values: new Map()
            });
        } else {
            this.setState({
                ready: false,
                values: null
            });

            restService
                .getScript(props.id)
                .then(script => {
                    this.setState({
                        values: new Map({
                            name: script.name,
                            methods: script.methods,
                            scriptBody: script.scriptBody
                        }),
                        ready: true
                    });
                });
        }
    };

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        const {isNew, id, onClose} = this.props;
        const data = this.state.values.toJS();

        //todo: validation & error display
        if (isNew) {
            restService
                .createScript(data)
                .then(script => {
                    onClose();
                    this.props.addScript(script);
                });
        } else {
            restService
                .updateScript(id, data)
                .then(script => {
                    onClose();
                    this.props.updateScript(script);
                });
        }
    };

    mutateValue = (field, value) => {
        this.setState(state => {
            return {
                values: state.values.set(field, value)
            };
        }, () => console.log(this.state.values.toJS()));
    };

    _setTextValue = (field) => (event) => this.mutateValue(field, event.target.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    render() {
        const {onClose, isNew} = this.props;
        const {ready, values} = this.state;

        let body = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            body =
                <form className="aui" onSubmit={this._onSubmit}>
                    <div className="field-group">
                        <label htmlFor="directory-dialog-name">
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
                        {/* todo: description */}
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.httpMethods}
                        </label>
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.scriptCode}
                            <AUIRequired/>
                        </label>
                        <Editor
                            mode="groovy"

                            onChange={this._setObjectValue('script')}
                            value={values.get('script') || ''}
                        />
                    </div>
                    {isNew && <div className="field-group">
                        <label>
                            {FieldMessages.comment}
                            <AUIRequired/>
                        </label>
                        <textarea
                            id={'rest-script-dialog-comment'}
                            className="textarea long-field"

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                    </div> }
                </form>;
        }

        //todo: delete

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
