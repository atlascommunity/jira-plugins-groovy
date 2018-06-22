//@flow
import React from 'react';

import Button from '@atlaskit/button';

import {RouterLink} from '../common/ak/RouterLink';


export class MainApp extends React.PureComponent<{}> {
    render() {
        return (
            <div>
                <Button
                    href="/registry"
                    component={RouterLink}
                >
                    Registry
                </Button>
                <Button
                    href="/admin-scripts"
                    component={RouterLink}
                >
                    Admin scripts
                </Button>
            </div>
        );
    }
}
