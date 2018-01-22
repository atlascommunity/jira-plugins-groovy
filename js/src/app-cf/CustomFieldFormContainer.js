import React from 'react';
import PropTypes from 'prop-types';

import Message from 'aui-react/lib/AUIMessage';

import {CustomFieldForm} from './CustomFieldForm';

import {Script} from '../common/Script';
import {fieldConfigService} from '../service/services';
import {ScriptFieldMessages} from '../i18n/cf.i18n';
import {CommonMessages, FieldMessages} from '../i18n/common.i18n';


export class CustomFieldFormContainer extends React.Component {
    static propTypes = {
        id: PropTypes.number.isRequired
    };

    state = {
        ready: false,
        editing: false,
        config: null
    };

    _setEditing = (value) => () => this.setState({ editing: value });

    _onChange = (value) => this.setState({ config: value, editing: false });

    componentDidMount() {
        if (Number.isNaN(this.props.id)) {
            this.setState({ ready: true });
        } else {
            fieldConfigService
                .getFieldConfig(this.props.id)
                .then(config => this.setState({config, ready: true}));
        }
    }

    render() {
        const {config, ready, editing} = this.state;

        if (!ready) {
            return <div className="aui-icon aui-icon-wait"/>;
        }

        if (Number.isNaN(this.props.id)) {
            return <Message type="error" title="Error">Incorrect config id</Message>;
        }

        return <div>
            <header className="aui-page-header">
                <div className="aui-page-header-inner">
                    <div className="aui-page-header-main">
                        <h2>{ScriptFieldMessages.scriptFor(`${config.customFieldName} - ${config.contextName}`)}</h2>
                    </div>
                    <div className="aui-page-header-actions">
                        {config.uuid && !editing && <button
                            className="aui-button"
                            onClick={this._setEditing(true)}
                        >
                            {CommonMessages.edit}
                        </button>}
                    </div>
                </div>
            </header>
            <div className="page-content">
                {config.uuid && !editing &&
                    <Script
                        script={{
                            id: config.uuid,
                            name: `${config.customFieldName} - ${config.contextName}`,
                            inline: true,
                            scriptBody: config.scriptBody,
                            changelogs: config.changelogs,
                        }}

                        withChangelog={true}
                        collapsible={false}
                        headerless={true}

                        onEdit={this._setEditing(true)}
                    >
                        <div>
                            <strong>{FieldMessages.cacheable}{':'}</strong> {config.cacheable ? 'yes' : 'no'}
                        </div>
                    </Script>
                }
                {(!config.uuid || editing) &&
                    <CustomFieldForm id={this.props.id} fieldConfig={config} onChange={this._onChange} onCancel={this._setEditing(false)}/>
                }
            </div>
        </div>;
    }
}
