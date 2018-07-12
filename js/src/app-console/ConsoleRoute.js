//@flow
import React, {type Node} from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs from '@atlaskit/breadcrumbs';

import {ScriptConsole} from './ScriptConsole';

import {TitleMessages} from '../i18n/common.i18n';
import {withRoot} from '../common/script-list/breadcrumbs';
import {ScrollToTop} from '../common/ScrollToTop';


export function ConsoleRoute(): Node {
    return (
        <Page>
            <PageHeader
                breadcrumbs={<Breadcrumbs>{withRoot([])}</Breadcrumbs>}
            >
                {TitleMessages.console}
            </PageHeader>
            <ScrollToTop/>
            <ScriptConsole/>
        </Page>
    );
}
