import React from 'react';
import PropTypes from 'prop-types';

import {Map} from 'immutable';

import Button from 'aui-react/lib/AUIButton';

import {fieldConfigService} from '../service/services';
import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {Editor} from '../common/Editor';
import {AUIRequired} from '../common/aui-components';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';


export class CustomFieldForm extends React.Component {
    static propTypes = {
        id: PropTypes.number.isRequired,
        fieldConfig: PropTypes.object.isRequired, //todo: shape
        onChange: PropTypes.func.isRequired,
        onCancel: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        const {fieldConfig} = props;

        this.state = {
            values: new Map({
                scriptBody: fieldConfig.scriptBody,
                cacheable: fieldConfig.cacheable,
                comment: fieldConfig.comment
            }),
            error: null
        };
    }

    _onSubmit = (e) => {
        if (e) {
            e.preventDefault();
        }

        fieldConfigService
            .updateFieldConfig(this.props.id, this.state.values.toJS())
            .then(
                (data) => this.props.onChange(data),
                (error) => {
                    const {response} = error;

                    if (response.status === 400) {
                        this.setState({error: response.data});
                    } else {
                        throw error;
                    }
                }
            );
    };

    _mutateValue = (field, value) => {
        this.setState(state => {
            return {
                values: state.values.set(field, value)
            };
        });
    };

    _setObjectValue = (field) => (value) => this._mutateValue(field, value);

    _setTextValue = (field) => (event) => this._mutateValue(field, event.target.value);

    _setToggleValue = (field) => e => this._mutateValue(field, e.target.checked);

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

        return (
            <form className="aui top-label" onSubmit={this._onSubmit}>
                <div className="field-group">
                    <div className="checkbox">
                        <input
                            className="checkbox"
                            type="checkbox"
                            id="field-form-cacheable"

                            onChange={this._setToggleValue('cacheable')}
                            checked={values.get('cacheable')}
                        />
                        <label htmlFor="field-form-cacheable">{FieldMessages.cacheable}</label>
                    </div>
                </div>
                <div className="field-group">
                    <label>{FieldMessages.scriptCode}</label>
                    <Editor
                        mode="groovy"
                        decorated={true}
                        bindings={[
                            Bindings.issue
                        ]}

                        value={values.get('scriptBody')}
                        onChange={this._setObjectValue('scriptBody')}

                        markers={markers}
                        annotations={annotations}
                    />
                    {errorField === 'scriptBody' && <div className="error">{errorMessage}</div>}
                </div>
                {this.props.fieldConfig.uuid &&
                    <div className="field-group">
                        <label htmlFor="field-form-comment">
                            {FieldMessages.comment}
                            <AUIRequired/>
                        </label>
                        <textarea
                            id="field-form-comment"
                            className="textarea full-width-field"

                            value={values.get('comment') || ''}
                            onChange={this._setTextValue('comment')}
                        />
                        {errorField === 'comment' && <div className="error">{errorMessage}</div>}
                    </div>
                }
                <div className="field-group">
                    <Button type="primary" onClick={this._onSubmit}>{CommonMessages.update}</Button>
                    {this.props.fieldConfig.uuid &&
                        <button className="aui-button aui-button-link" onClick={this.props.onCancel}>{CommonMessages.cancel}</button>
                    }
                </div>
            </form>
        );
    }
}
