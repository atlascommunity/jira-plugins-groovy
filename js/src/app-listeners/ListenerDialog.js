import React from 'react';
import PropTypes from 'prop-types';

import {Map} from 'immutable';

import Button from 'aui-react/lib/AUIButton';
import Modal from 'aui-react/lib/AUIDialog';

import {ConditionPicker} from './ConditionPicker';

import {AUIRequired} from '../common/aui-components';
import {ListenerMessages} from '../i18n/listener.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';
import {listenerService} from '../service/services';
import {Editor} from '../common/Editor';


export class ListenerDialog extends React.Component {
    static propTypes = {
        isNew: PropTypes.bool.isRequired,
        onClose: PropTypes.func.isRequired,
        id: PropTypes.number,
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

    _fillConditionKeys(condition) {
        if (condition.children) {
            let i = 0;

            condition.children = condition
                .children
                .map(child => {
                    this._fillConditionKeys(child);

                    return {
                        ...child,
                        key: i++
                    };
                });
        }
        return condition;
    }

    _init = props => {
        if (props.isNew) {
            this.setState({
                ready: true,
                values: new Map({
                    condition: {
                        key: 0
                    }
                })
            });
        } else {
            this.setState({
                ready: false,
                values: null
            });

            listenerService
                .getListener(props.id)
                .then(listener =>
                    this.setState({
                        values: new Map({
                            name: listener.name,
                            script: listener.script,
                            condition: this._fillConditionKeys(listener.condition)
                        }),
                        ready: true
                    })
                );
        }
    };

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        const {isNew, id} = this.props;
        const data = this.state.values.toJS();

        if (isNew) {
            listenerService
                .createListener(data)
                .then(listener => console.log(listener));
        } else {
            listenerService
                .updateListener(id, data)
                .then(listener => console.log(listener));
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
                            id="directory-dialog-name"
                            value={values.get('name') || ''}
                            onChange={this._setTextValue('name')}
                        />
                    </div>
                    <div className="field-group">
                        <label>
                            {FieldMessages.condition}
                            <AUIRequired/>
                        </label>
                        <ConditionPicker value={values.get('condition')} onChange={this._setObjectValue('condition')}/>
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
                </form>;
        }

        //todo: add condition display
        //todo: delete

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
