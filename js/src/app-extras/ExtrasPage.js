//@flow
import React from 'react';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs from '@atlaskit/breadcrumbs';

import {extrasService} from '../service/services';
import {CommonMessages, TitleMessages} from '../i18n/common.i18n';
import {withRoot} from '../common/script-list/breadcrumbs';


export class ExtrasPage extends React.Component<{}> {
    _clearCache = () => {
        extrasService.clearCache().then(() => alert('done'));
    };

    render() {
        return <Page>
            <PageHeader
                breadcrumbs={<Breadcrumbs>{withRoot([])}</Breadcrumbs>}
            >
                {TitleMessages.extras}
            </PageHeader>
            <div className="page-content">
                <Button appearance="primary" onClick={this._clearCache}>{CommonMessages.clearCache}</Button>
            </div>
        </Page>;
    }
}
