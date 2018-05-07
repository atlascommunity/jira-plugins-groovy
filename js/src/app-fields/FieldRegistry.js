import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import Script, {ScriptParameters} from '../common/script';
import {FieldError} from '../common/ak/FieldError';
import {LoadingSpinner} from '../common/ak/LoadingSpinner';
import {InfoMessage} from '../common/ak/messages';

import {getBaseUrl} from '../service/ajaxHelper';

import {JiraMessages, FieldMessages, ErrorMessages, CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ScriptFieldMessages} from '../i18n/cf.i18n';


@connect(
    state => {
        return {
            configs: state.scripts,
            ready: state.ready
        };
    }
)
export class FieldRegistry extends React.PureComponent {
    static propTypes = {
        configs: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
        ready: PropTypes.bool.isRequired
    };

    render() {
        const {configs, ready} = this.props;

        if (!ready) {
            return <LoadingSpinner/>;
        }

        return <Page>
            <PageHeader>
                {TitleMessages.fields}
            </PageHeader>
            <div className="page-content ScriptList">
                {!!configs.length && configs.map(config =>
                    <Field key={config.id} config={config}/>
                )}

                {!configs.length && <InfoMessage title={ScriptFieldMessages.noFields}/>}
            </div>
        </Page>;
    }
}

//todo: velocity vars, searcher
class Field extends React.PureComponent {
    static propTypes = {
        config: PropTypes.object.isRequired
    };

    render() {
        const {config} = this.props;

        return (
            <Script
                script={{
                    id: config.uuid,
                    name: `${config.customFieldName} - ${config.contextName}`,
                    inline: true,
                    scriptBody: config.scriptBody,
                    changelogs: config.changelogs,
                    errorCount: config.errorCount
                }}
                template={config.needsTemplate ? {
                    body: config.template
                } : null}

                withChangelog={true}

                dropdownItems={[
                    {
                        label: `${JiraMessages.edit} ${CommonMessages.script}`,
                        href: `${getBaseUrl()}/plugins/servlet/my-groovy/custom-field?fieldConfigId=${config.id}`
                    },
                    {
                        label: `${JiraMessages.edit} ${FieldMessages.customField}`,
                        href: `${getBaseUrl()}/secure/admin/EditCustomField!default.jspa?id=${config.customFieldId}`
                    },
                    {
                        label: `${JiraMessages.configure} ${FieldMessages.customField}`,
                        href: `${getBaseUrl()}/secure/admin/ConfigureCustomField!default.jspa?customFieldId=${config.customFieldId}`
                    }
                ]}
            >
                {!config.uuid && <FieldError error={ErrorMessages.notConfigured}/>}
                <ScriptParameters
                    params={[
                        {
                            label: FieldMessages.type,
                            value: config.type
                        },
                        {
                            label: FieldMessages.searcher,
                            value: config.searcher || CommonMessages.no
                        },
                        {
                            label: FieldMessages.cacheable,
                            value: config.cacheable ? CommonMessages.yes : CommonMessages.no
                        },
                    ]}
                />
            </Script>
        );
    }
}
