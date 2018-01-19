import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Icon from 'aui-react/lib/AUIIcon';

import {Script} from '../common/Script';
import {getBaseUrl} from '../service/ajaxHelper';
import {JiraMessages, FieldMessages, ErrorMessages, CommonMessages, TitleMessages} from '../i18n/common.i18n';


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
            return <div className="aui-icon aui-icon-wait"/>;
        }

        return <div>
            <header className="aui-page-header">
                <div className="aui-page-header-inner">
                    <div className="aui-page-header-main">
                        <h2>{TitleMessages.fields}</h2>
                    </div>
                </div>
            </header>
            <div className="page-content">
                {configs && configs.map(config =>
                    <Field key={config.id} config={config}/>
                )}
            </div>
        </div>;
    }
}

class Field extends React.Component {
    static propTypes = {
        config: PropTypes.object.isRequired
    };

    render() {
        const {config} = this.props;

        const actionsElId = `field-${config.id}-actions`;

        return <div>
            <Script
                script={{
                    id: config.uuid,
                    name: `${config.customFieldName} - ${config.contextName}`,
                    inline: true,
                    scriptBody: config.scriptBody,
                    changelogs: config.changelogs,
                }}

                withChangelog={true}

                additionalButtons={
                    <button type="button" className="aui-button aui-button-subtle aui-dropdown2-trigger" aria-owns={actionsElId}>
                        <Icon icon="configure"/>

                        <div id={actionsElId} className="aui-dropdown2 aui-style-default">
                            <ul className="aui-list-truncate">
                                <li>
                                    <a href={`${getBaseUrl()}/plugins/servlet/my-groovy/custom-field?fieldConfigId=${config.id}`}>
                                        {JiraMessages.edit}{' '}{CommonMessages.script}
                                    </a>
                                </li>
                                <li>
                                    <a href={`${getBaseUrl()}/secure/admin/EditCustomField!default.jspa?id=${config.customFieldId}`}>
                                        {JiraMessages.edit}{' '}{FieldMessages.customField}
                                    </a>
                                </li>
                                <li>
                                    <a href={`${getBaseUrl()}/secure/admin/ConfigureCustomField!default.jspa?customFieldId=${config.customFieldId}`}>
                                        {JiraMessages.configure}{' '}{FieldMessages.customField}
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </button>
                }
            >
                <div className="field-group">
                    <label>{FieldMessages.cacheable}{':'}</label>
                    {config.cacheable ? CommonMessages.yes : CommonMessages.no}
                </div>
                {!config.uuid && <div>
                    <strong style={{color: 'red'}}>
                        {ErrorMessages.notConfigured}
                    </strong>
                </div>}
            </Script>
        </div>;
    }
}