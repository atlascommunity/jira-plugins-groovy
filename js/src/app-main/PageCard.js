//@flow
import React, {type Node} from 'react';

import {RouterLink} from '../common/ak/RouterLink';

import './PageCard.less';


type Props = {
    title: string,
    href: string,
    description?: string
};

export function PageCard({title, href, description}: Props): Node {
    return (
        <RouterLink href={href} className="PageCard">
            <h3>
                {title}
            </h3>
            {description &&
                <div className="muted-text">
                    {description}
                </div>
            }
        </RouterLink>
    );
}
