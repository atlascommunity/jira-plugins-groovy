import React from 'react';
import PropTypes from 'prop-types';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {RestScript} from './RestScript';

import {InfoMessage} from '../common/ak/messages';

import {RestMessages} from '../i18n/rest.i18n';
import {TitleMessages} from '../i18n/common.i18n';


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
