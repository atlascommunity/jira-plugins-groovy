//@flow
import * as React from 'react';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {CustomFieldForm} from './CustomFieldForm';
import type {FieldConfig} from './types';

import Script, {ScriptParameters} from '../common/script';
import {LoadingSpinner} from '../common/ak/LoadingSpinner';

import {fieldConfigService} from '../service/services';

import {ScriptFieldMessages} from '../i18n/cf.i18n';
import {CommonMessages, ErrorMessages, FieldMessages} from '../i18n/common.i18n';
import {ErrorMessage} from '../common/ak/messages';


type Props = {
    id: number
};

type State = {
    ready: boolean,
    editing: boolean,
    config: ?FieldConfig
};

export class CustomFieldFormContainer extends React.Component<Props, State> {
    state = {
        ready: false,
        editing: false,
        config: null
    };

    _setEditing = (value: boolean) => () => this.setState({ editing: value });

    _onChange = (value: FieldConfig) => this.setState({ config: value, editing: false });

    componentDidMount() {
        if (Number.isNaN(this.props.id)) {
            this.setState({ ready: true });
        } else {
            fieldConfigService
                .getFieldConfig(this.props.id)
                .then(config => this.setState({config, ready: true}));
        }
    }

    render(): React.Node {
        const {config, ready, editing} = this.state;

        if (!ready || !config) {
            return <LoadingSpinner/>;
        }

        if (Number.isNaN(this.props.id)) {
            return (
                <div style={{ marginTop: '20px' }}>
                    <ErrorMessage title={ErrorMessages.incorrectConfigId}/>
                </div>
            );
        }

        let actions: ?React.Element<any> = null;

        if (config.uuid && !editing) {
            actions = (
                <Button
                    appearance="primary"
                    onClick={this._setEditing(true)}
                >
                    {CommonMessages.edit}
                </Button>
            );
        }

        return <Page>
            <PageHeader actions={actions || undefined}>
                {ScriptFieldMessages.scriptFor(`${config.customFieldName} - ${config.contextName}`)}
            </PageHeader>

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
                        template={config.needsTemplate ? { body: (config.template || '') }: undefined}

                        withChangelog={true}
                        collapsible={false}
                        headerless={true}

                        onEdit={this._setEditing(true)}
                    >
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
                                    value: config.cacheable ? 'yes' : 'no'
                                },
                            ]}
                        />
                    </Script>
                }
                {(!config.uuid || editing) &&
                    <CustomFieldForm id={this.props.id} fieldConfig={config} onChange={this._onChange} onCancel={this._setEditing(false)}/>
                }
            </div>
        </Page>;
    }
}
