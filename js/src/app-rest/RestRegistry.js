import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {ScriptActionCreators} from './rest.reducer';

import {Script, ScriptParameters} from '../common/Script';
import {InfoMessage} from '../common/ak/messages';

import {restService} from '../service/services';
import {getPluginBaseUrl} from '../service/ajaxHelper';

import {RestMessages} from '../i18n/rest.i18n';
import {CommonMessages, FieldMessages, TitleMessages} from '../i18n/common.i18n';


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
                    {!scripts.length && <InfoMessage title={RestMessages.noScripts}/>}
                </div>
            </Page>
        );
    }
}


@connect(null, ScriptActionCreators)
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
                <ScriptParameters
                    params={[
                        {
                            label: 'URL',
                            value: <a href={url}>{url}</a>
                        },
                        {
                            label: FieldMessages.httpMethods,
                            value: script.methods.join(', ')
                        },
                        {
                            label: FieldMessages.groups,
                            value: script.groups.length ?
                                script.groups.join(', ') :
                                <div className="muted-text">{CommonMessages.notSpecified}</div>
                        }
                    ]}
                />
            </Script>
        );
    }
}
