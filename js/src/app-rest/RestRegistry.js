import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Button from 'aui-react/lib/AUIButton';
import Message from 'aui-react/lib/AUIMessage';

import {ScriptActionCreators} from './rest.reducer';

import {restService} from '../service/services';
import {RestMessages} from '../i18n/rest.i18n';
import {Script} from '../common/Script';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {FieldMessages, TitleMessages} from '../i18n/common.i18n';
import {ListenerMessages} from '../i18n/listener.i18n';


export class RestRegistry extends React.Component {
    static propTypes = {
        scripts: PropTypes.arrayOf(PropTypes.object).isRequired, //todo: model
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew, id) => () => this.props.triggerDialog(isNew, id);

    render() {
        const {scripts} = this.props;

        return (
            <div>
                <header className="aui-page-header">
                    <div className="aui-page-header-inner">
                        <div className="aui-page-header-main">
                            <h2>{TitleMessages.rest}</h2>
                        </div>
                        <div className="aui-page-header-actions">
                            <Button onClick={this._triggerDialog(true)}>
                                {RestMessages.addScript}
                            </Button>
                        </div>
                    </div>
                </header>
                <div className="flex-column page-content">
                    {scripts.map(script =>
                        <RestScript
                            key={script.id}
                            script={script}
                            onEdit={this._triggerDialog(false, script.id)}
                        />
                    )}
                    {!scripts.length && <Message type="info">{RestMessages.noScripts}</Message>}
                </div>
            </div>
        );
    }
}


@connect(
    _state => { return {}; },
    ScriptActionCreators
)
class RestScript extends React.Component {
    static propTypes = {
        script: PropTypes.object.isRequired, //todo: model
        onEdit: PropTypes.func.isRequired,
        deleteScript: PropTypes.func.isRequired
    };

    _delete = () => {
        const script = this.props.script;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${script.name}"?`)) {
            restService.deleteScript(script.id).then(() => this.props.deleteScript(script.id));
        }
    };

    render() {
        const {script, onEdit} = this.props;

        const url = `${getPluginBaseUrl()}/custom/${script.name}`;

        return <div className="flex-column">
            <Script
                script={{
                    id: script.uuid,
                    name: script.name,
                    inline: true,
                    scriptBody: script.scriptBody,
                    changelogs: script.changelogs
                }}

                withChangelog={true}

                onEdit={onEdit}
                onDelete={this._delete}
            >
                <div className="flex-column">
                    <div>
                        <a href={url}>{url}</a>
                    </div>
                    <div>
                        <strong>{FieldMessages.httpMethods}{':'}</strong> {script.methods.join(', ')}
                    </div>
                </div>
            </Script>
        </div>;
    }
}
