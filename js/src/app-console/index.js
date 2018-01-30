import ReactDOM from 'react-dom';
import React from 'react';

import '../flex.less';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {ScriptConsole} from './ScriptConsole';

import {TitleMessages} from '../i18n/common.i18n';

import {fixStyle} from '../common/fixStyle';


AJS.toInit(() => {
    fixStyle();
    ReactDOM.render(
        <Page>
            <PageHeader>
                {TitleMessages.console}
            </PageHeader>
            <ScriptConsole/>
        </Page>,
        document.getElementById('react-content')
    );
});
