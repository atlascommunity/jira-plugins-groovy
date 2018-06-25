//@flow
import React from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {PageCard} from './PageCard';

import './MainApp.less';


export class MainApp extends React.PureComponent<{}> {
    render() {
        return (
            <Page>
                <PageHeader>Mail.ru Groovy</PageHeader>
                <div className="MainApp">
                    <PageCard
                        href="/console"
                        title="Console"
                    />
                    <PageCard
                        href="/admin-scripts"
                        title="Admin scripts"
                    />
                    <PageCard
                        href="/registry"
                        title="Registry"
                    />
                    <PageCard
                        href="/listeners"
                        title="Listeners"
                    />
                    <PageCard
                        href="/rest"
                        title="REST scripts"
                    />
                    <PageCard
                        href="/fields"
                        title="Scripted fields"
                    />
                    <PageCard
                        href="/scheduled"
                        title="Scheduled tasks"
                    />
                    <PageCard
                        href="/audit"
                        title="Audit log"
                    />
                    <PageCard
                        href="/extras"
                        title="Extras"
                    />
                </div>
            </Page>
        );
    }
}
