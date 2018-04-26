//@flow
import * as React from 'react';

import Spinner from '@atlaskit/spinner';

import './LoadingSpinner.less';


export function LoadingSpinner(): React.Node {
    return (
        <div className="LoadingSpinner">
            <div className="flex-horizontal-middle">
                <Spinner size="medium"/>
            </div>
        </div>
    );
}
