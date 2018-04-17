import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Message from 'aui-react/lib/AUIMessage';

import DropdownMenu, {DropdownItem, DropdownItemGroup} from '@atlaskit/dropdown-menu';
import Spinner from '@atlaskit/spinner';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import SettingsIcon from '@atlaskit/icon/glyph/settings';

import {Script, ScriptParameters} from '../common/Script';
import {getBaseUrl} from '../service/ajaxHelper';
import {JiraMessages, FieldMessages, ErrorMessages, CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {ScriptFieldMessages} from '../i18n/cf.i18n';
import {FieldError} from '../common/ak/FieldError';


@connect(
    state => {
        return {
            configs: state.scripts,
            ready: state.ready
        };
    }
)
export class FieldRegistry extends React.Component {
    static propTypes = {
        configs: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
        ready: PropTypes.bool.isRequired
    };

    render() {
        const {configs, ready} = this.props;

        if (!ready) {
            return <Spinner size="medium" />;
        }

        return <Page>
            <PageHeader>
                {TitleMessages.fields}
            </PageHeader>
            <div className="page-content ScriptList">
                {!!configs.length && configs.map(config =>
                    <Field key={config.id} config={config}/>
                )}

                {!configs.length && <Message type="info" title={ScriptFieldMessages.noFields}>{ScriptFieldMessages.noFields}</Message>}
            </div>
        </Page>;
    }
}

//todo: velocity vars, searcher
class Field extends React.Component {
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
                template={config.needsTemplate && {
                    body: config.template
                }}

                withChangelog={true}

                additionalButtons={
                    <div style={{marginLeft: '5px'}}>
                        <DropdownMenu
                            triggerType="button"
                            triggerButtonProps={{appearance: 'subtle', iconBefore: <SettingsIcon label=""/>}}

                            position="bottom right"
                        >
                            <DropdownItemGroup>
                                <DropdownItem href={`${getBaseUrl()}/plugins/servlet/my-groovy/custom-field?fieldConfigId=${config.id}`}>
                                    {JiraMessages.edit}{' '}{CommonMessages.script}
                                </DropdownItem>
                                <DropdownItem href={`${getBaseUrl()}/secure/admin/EditCustomField!default.jspa?id=${config.customFieldId}`}>
                                    {JiraMessages.edit}{' '}{FieldMessages.customField}
                                </DropdownItem>
                                <DropdownItem href={`${getBaseUrl()}/secure/admin/ConfigureCustomField!default.jspa?customFieldId=${config.customFieldId}`}>
                                    {JiraMessages.configure}{' '}{FieldMessages.customField}
                                </DropdownItem>
                            </DropdownItemGroup>
                        </DropdownMenu>
                    </div>
                }
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
