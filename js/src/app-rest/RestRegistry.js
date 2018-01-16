import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Button from 'aui-react/lib/AUIButton';

import {ScriptActionCreators} from './rest.reducer';

import {restService} from '../service/services';
import {RestMessages} from '../i18n/rest.i18n';
import {Script} from '../common/Script';
import {getBaseUrl, getPluginBaseUrl} from '../service/ajaxHelper';
import {FieldMessages} from '../i18n/common.i18n';


export class RestRegistry extends React.Component {
    static propTypes = {
        scripts: PropTypes.arrayOf(PropTypes.object).isRequired, //todo: model
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew, id) => () => this.props.triggerDialog(isNew, id);

    render() {
        return (
            <div className="flex-column">
                <div>
                    <Button icon="add" type="primary" onClick={this._triggerDialog(true)}>
                        {RestMessages.addScript}
                    </Button>
                </div>
                <div className="flex-column">
                    {this.props.scripts.map(script =>
                        <RestScript
                            key={script.id}
                            script={script}
                            onEdit={this._triggerDialog(false, script.id)}
                        />
                    )}
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
                editable={true}
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
