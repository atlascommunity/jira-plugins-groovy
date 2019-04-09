//@flow
import React, {type Node} from 'react';

import DynamicTable from '@atlaskit/dynamic-table';
import Lozenge from '@atlaskit/lozenge';
import Badge from '@atlaskit/badge';


export type CustomRenderer = (any) => Node;

export const customRenderers: {[string]: CustomRenderer} = {
    recentErrors: (items: $ReadOnlyArray<any>): Node => {
        return (
            <DynamicTable
                rowsPerPage={20}
                head={{
                    cells: [
                        {
                            key: 'type',
                            content: 'Type',
                            isSortable: true
                        },
                        {
                            content: 'Name'
                        },
                        {
                            key: 'errorCount',
                            content: 'Errors',
                            isSortable: true
                        },
                        {
                            key: 'lastError',
                            content: 'Last error',
                            isSortable: true,
                            with: 140
                        }
                    ]
                }}
                rows={
                    items.map(item => ({
                        cells: [
                            {
                                key: item.type || 'UNKNOWN',
                                content: (
                                    <Lozenge
                                        appearance={item.type ? 'new' : 'default'}
                                        isBold={true}
                                    >
                                        {item.type || 'UNKNOWN'}
                                    </Lozenge>
                                )
                            },
                            {
                                content: item.url ? <a href={item.url}>{item.name}</a> : item.name
                            },
                            {
                                key: item.errorCount,
                                content: (
                                    <Badge
                                        appearance="important"
                                        value={item.errorCount}
                                    />
                                )
                            },
                            {
                                key: item.lastErrorTimestamp,
                                content: item.lastErrorDate
                            }
                        ]
                    }))
                }
            />
        );
    }
};

