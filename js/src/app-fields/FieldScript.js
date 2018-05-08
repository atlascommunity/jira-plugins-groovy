//@flow
import * as React from 'react';

import type {FieldConfigItem} from './types';

import {getBaseUrl} from '../service/ajaxHelper';
import {Script} from '../common/script/Script';

import {JiraMessages, FieldMessages, ErrorMessages, CommonMessages} from '../i18n/common.i18n';
import {FieldError} from '../common/ak/FieldError';
import {ScriptParameters} from '../common/script';


type Props = {
    script: FieldConfigItem,
    onEdit: any //ignored
};

export class FieldScript extends React.PureComponent<Props> {
    render(): React.Node {
        const {script} = this.props;

        return (
            <Script
                script={{
                    id: script.uuid,
                    name: `${script.customFieldName} - ${script.contextName}`,
                    inline: true,
                    scriptBody: script.scriptBody,
                    changelogs: script.changelogs,
                    errorCount: script.errorCount
                }}
                template={script.needsTemplate ? {
                    body: script.template || ''
                } : undefined}

                withChangelog={true}

                dropdownItems={[
                    {
                        label: `${JiraMessages.edit} ${CommonMessages.script}`,
                        href: `${getBaseUrl()}/plugins/servlet/my-groovy/custom-field?fieldConfigId=${script.id}`
                    },
                    {
                        label: `${JiraMessages.edit} ${FieldMessages.customField}`,
                        href: `${getBaseUrl()}/secure/admin/EditCustomField!default.jspa?id=${script.customFieldId}`
                    },
                    {
                        label: `${JiraMessages.configure} ${FieldMessages.customField}`,
                        href: `${getBaseUrl()}/secure/admin/ConfigureCustomField!default.jspa?customFieldId=${script.customFieldId}`
                    }
                ]}
            >
                {!script.uuid && <FieldError error={ErrorMessages.notConfigured}/>}
                <ScriptParameters
                    params={[
                        {
                            label: FieldMessages.type,
                            value: script.type
                        },
                        {
                            label: FieldMessages.searcher,
                            value: script.searcher || CommonMessages.no
                        },
                        {
                            label: FieldMessages.cacheable,
                            value: script.cacheable ? CommonMessages.yes : CommonMessages.no
                        },
                    ]}
                />
            </Script>
        );
    }
}
