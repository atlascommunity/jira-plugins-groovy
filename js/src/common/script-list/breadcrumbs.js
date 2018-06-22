//@flow
import React, {type Element} from 'react';

import {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import {RouterLink} from '../ak/RouterLink';


export const withRoot = (breadcrumbs: Array<?Element<typeof BreadcrumbsItem>>): Array<?Element<typeof BreadcrumbsItem>> => [
    <BreadcrumbsItem
        key="root"
        text="Mail.Ru Groovy"

        //$FlowFixMe
        component={RouterLink}
        href="/"
    />,
    ...breadcrumbs
];
