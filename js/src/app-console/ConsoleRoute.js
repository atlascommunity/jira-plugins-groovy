//@flow
import React from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs from '@atlaskit/breadcrumbs';

import {ScriptConsole} from './ScriptConsole';

import {TitleMessages} from '../i18n/common.i18n';
import {withRoot} from '../common/script-list/breadcrumbs';


export class ConsoleRoute extends React.Component<{}> {
    render() {
        return (
            <Page>
                <PageHeader
                    breadcrumbs={<Breadcrumbs>{withRoot([])}</Breadcrumbs>}
                >
                    {TitleMessages.console}
                </PageHeader>
                <ScriptConsole/>
            </Page>
        );
    }
}
