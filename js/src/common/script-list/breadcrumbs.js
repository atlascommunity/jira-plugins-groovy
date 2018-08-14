//@flow
import React, {type Element} from 'react';

import {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import {RouterLink} from '../ak/RouterLink';


export const withRoot = (breadcrumbs: Array<?Element<typeof BreadcrumbsItem>>): Array<?Element<typeof BreadcrumbsItem>> => [
    <BreadcrumbsItem
        key="root"
        text="MyGroovy"

        component={RouterLink}
        href="/"
    />,
    ...breadcrumbs
];
