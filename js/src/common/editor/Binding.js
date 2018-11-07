//@flow
import React, {type Node} from 'react';

import Tooltip from '@atlaskit/tooltip';

import type {BindingType} from './types';


type BindingProps = {
    binding: BindingType
};

export function Binding({binding}: BindingProps): Node {
    return (
        <div className="flex-row">
            <div className="flex-none">{binding.name}</div>
            <div className="flex-grow"/>
            <div className="flex-none" style={{marginLeft: '5px'}}>
                <Tooltip content={<code>{binding.fullClassName}</code>}>
                    {binding.javaDoc
                        ? (
                            <a
                                href={binding.javaDoc}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                {binding.className}
                            </a>
                        )
                        : <abbr>{binding.className}</abbr>
                    }
                </Tooltip>
            </div>
        </div>
    );
}
