//@flow
import React, {Fragment} from 'react';
import {Link} from 'react-router-dom';
import {connect} from 'react-redux';

import memoizeOne from 'memoize-one';

import Button from '@atlaskit/button';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';

import type {FieldConfigItem} from './types';

import {getBaseUrl} from '../service';

import {JiraMessages, FieldMessages, ErrorMessages, CommonMessages} from '../i18n/common.i18n';
import {ErrorMessage} from '../common/ak/messages';
import {ScriptParameters} from '../common/script';
import {WatchableScript} from '../common/script/WatchableScript';
import {WatchActionCreators} from '../common/redux';

import type {ScriptComponentProps} from '../common/script-list/types';
import {RouterLink} from '../common/ak/RouterLink';


const ConnectedWatchableScript = connect(
    memoizeOne(state => ({ watches: state.watches }) ),
    WatchActionCreators
)(WatchableScript);

type Props = ScriptComponentProps<FieldConfigItem>;

export class FieldScript extends React.PureComponent<Props> {
    render() {
        const {script, collapsible, focused} = this.props;

        return (
            <ConnectedWatchableScript
                entityId={script.id}
                entityType="CUSTOM_FIELD"

                script={{
                    id: script.uuid,
                    name: script.name,
                    inline: true,
                    scriptBody: script.scriptBody,
                    description: script.description,
                    changelogs: script.changelogs,
                    errorCount: script.errorCount,
                    warningCount: script.warningCount
                }}
                template={script.needsTemplate
                    ? {
                        body: script.template || ''
                    }
                    : undefined
                }

                withChangelog={true}
                collapsible={collapsible}
                focused={focused}

                scriptName={
                    <Fragment>
                        {script.customFieldName}
                        {' '}
                        <span className="muted-text">
                            {script.contextName}
                        </span>
                    </Fragment>
                }

                dropdownItems={[
                    {
                        label: CommonMessages.permalink,
                        href: `/fields/${script.id}/view`,
                        linkComponent: RouterLink
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

                additionalButtons={[
                    <Button
                        key="edit"
                        appearance="subtle"
                        iconBefore={<EditFilledIcon label=""/>}

                        component={Link}
                        to={`/fields/${script.id}/edit`}
                    />
                ]}
            >
                {!script.uuid && <ErrorMessage title={ErrorMessages.notConfigured}/>}
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
                        script.needsTemplate
                            ? {
                                label: 'Velocity params',
                                value: script.velocityParamsEnabled ? CommonMessages.yes : CommonMessages.no
                            }
                            : null
                    ]}
                />
            </ConnectedWatchableScript>
        );
    }
}
