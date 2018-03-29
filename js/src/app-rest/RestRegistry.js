import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Message from 'aui-react/lib/AUIMessage';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {ScriptActionCreators} from './rest.reducer';

import {restService} from '../service/services';
import {RestMessages} from '../i18n/rest.i18n';
import {Script} from '../common/Script';
import {getPluginBaseUrl} from '../service/ajaxHelper';
import {FieldMessages, TitleMessages} from '../i18n/common.i18n';


export class RestRegistry extends React.Component {
    static propTypes = {
        scripts: PropTypes.arrayOf(PropTypes.object).isRequired, //todo: shape
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew, id) => () => this.props.triggerDialog(isNew, id);

    render() {
        const {scripts} = this.props;

        return (
            <Page>
                <PageHeader
                    actions={
                        <Button appearance="primary" onClick={this._triggerDialog(true)}>
                            {RestMessages.addScript}
                        </Button>
                    }
                >
                    {TitleMessages.rest}
                </PageHeader>

                <div className="ScriptList page-content">
                    {scripts.map(script =>
                        <RestScript
                            key={script.id}
                            script={script}
                            onEdit={this._triggerDialog(false, script.id)}
                        />
                    )}
                    {!scripts.length && <Message type="info">{RestMessages.noScripts}</Message>}
                </div>
            </Page>
        );
    }
}


@connect(
    _state => { return {}; },
    ScriptActionCreators
)
class RestScript extends React.Component {
    static propTypes = {
        script: PropTypes.object.isRequired, //todo: shape
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

        return (
            <Script
                script={{
                    id: script.uuid,
                    name: script.name,
                    inline: true,
                    scriptBody: script.scriptBody,
                    changelogs: script.changelogs,
                    errorCount: script.errorCount
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
        );
    }
}
