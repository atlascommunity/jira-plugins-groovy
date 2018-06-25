//@flow
import React from 'react';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {RouterLink} from '../common/ak/RouterLink';


export class MainApp extends React.PureComponent<{}> {
    render() {
        return (
            <Page>
                <PageHeader>Mail.ru Groovy</PageHeader>
                <div className="flex-row">
                    <Button
                        href="/console"
                        component={RouterLink}
                    >
                        Console
                    </Button>
                    <Button
                        href="/admin-scripts"
                        component={RouterLink}
                    >
                        Admin scripts
                    </Button>
                    <Button
                        href="/registry"
                        component={RouterLink}
                    >
                        Registry
                    </Button>
                    <Button
                        href="/listeners"
                        component={RouterLink}
                    >
                        Listeners
                    </Button>
                    <Button
                        href="/rest"
                        component={RouterLink}
                    >
                        REST scripts
                    </Button>
                    <Button
                        href="/fields"
                        component={RouterLink}
                    >
                        Scripted fields
                    </Button>
                    <Button
                        href="/scheduled"
                        component={RouterLink}
                    >
                        Scheduled tasks
                    </Button>
                    <Button
                        href="/audit"
                        component={RouterLink}
                    >
                        Audit log
                    </Button>
                    <Button
                        href="/extras"
                        component={RouterLink}
                    >
                        Extras
                    </Button>
                </div>
            </Page>
        );
    }
}
