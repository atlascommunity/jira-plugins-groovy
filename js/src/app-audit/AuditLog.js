//@flow
import React, {type Element} from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Tooltip from '@atlaskit/tooltip';
import Avatar, {AvatarItem} from '@atlaskit/avatar';
import Lozenge from '@atlaskit/lozenge';
import Button from '@atlaskit/button';
import {DynamicTableStateless} from '@atlaskit/dynamic-table';
import Pagination from '@atlaskit/pagination';
import Breadcrumbs from '@atlaskit/breadcrumbs';

import type {RowType} from '@atlaskit/dynamic-table/dist/cjs/types';

import UndoIcon from '@atlaskit/icon/glyph/undo';

import {ActionIcon} from './ActionIcon';
import {AuditLogFilter} from './AuditLogFilter';

import type {AuditLogEntry, AuditLogData, AuditLogFilterType} from './types';

import {
    adminScriptService,
    auditLogService,
    listenerService,
    registryService,
    restService,
    scheduledTaskService
} from '../service/services';
import {CommonMessages, FieldMessages, TitleMessages} from '../i18n/common.i18n';
import {AuditMessages, CategoryNameMessages} from '../i18n/audit.i18n';

import {type EntityType} from '../common/types';
import {InfoMessage} from '../common/ak';
import {withRoot} from '../common/script-list/breadcrumbs';


const tableHead = {
    cells: [
        {
            content: FieldMessages.date,
            width: '120px'
        },
        {
            content: AuditMessages.user,
            width: '200px'
        },
        {
            content: '',
            width: '30px'
        },
        {
            content: AuditMessages.script,
            width: '250px'
        },
        {
            content: AuditMessages.description
        },
        {
            content: '',
            width: '35px'
        }
    ]
};


type Props = {

};

type State = {
    filter: AuditLogFilterType,
    offset: number,
    isReady: boolean,
    rows: Array<RowType>,
    data: AuditLogData
};

export class AuditLog extends React.Component<Props, State> {
    state = {
        offset: 0,
        isReady: false,
        rows: [],
        data: {
            offset: 0,
            limit: 1,
            total: 0,
            size: 0,
            isLast: true,
            values: []
        },
        filter: {
            users: [],
            categories: [],
            actions: []
        }
    };

    _restore = (category: EntityType, id: number) => () => {
        let promise: ?Promise<void> = null;
        switch (category) {
            case 'ADMIN_SCRIPT':
                promise = adminScriptService.restoreScript(id);
                break;
            case 'REGISTRY_SCRIPT':
                promise = registryService.restoreScript(id);
                break;
            case 'REGISTRY_DIRECTORY':
                promise = registryService.restoreDirectory(id);
                break;
            case 'LISTENER':
                promise = listenerService.restoreListener(id);
                break;
            case 'REST':
                promise = restService.restoreScript(id);
                break;
            case 'SCHEDULED_TASK':
                promise = scheduledTaskService.restore(id);
                break;
            default:
                console.error('unknown category', category);
        }

        if (promise) {
            this.setState({ isReady: false });

            promise
                .then(
                    () => this._loadList(this.state.offset, this.state.filter),
                    (error: *) => {
                        this.setState({ isReady: true });
                        throw error;
                    }
                );
        }
    };

    _loadList(offset: number, filter: AuditLogFilterType) {
        this.setState({
            isReady: false
        });

        auditLogService
            .getAuditLogPage(offset, filter.users.map(it => it.value), filter.categories, filter.actions)
            .then(data => this.setState({
                data,
                rows: data.values.map((value: AuditLogEntry): RowType => {
                    return {
                        key: value.id.toString(10),
                        cells: [
                            {
                                content: value.date
                            },
                            {
                                content: (
                                    <AvatarItem
                                        backgroundColor="transparent"
                                        avatar={<Avatar src={value.user.avatarUrl} appearance="square"/>}

                                        primaryText={value.user.displayName}
                                        secondaryText={value.user.name}
                                    />
                                )
                            },
                            {
                                content: <ActionIcon action={value.action}/>
                            },
                            {
                                content: (
                                    <div className="flex-column">
                                        <div>
                                            <Lozenge>
                                                {CategoryNameMessages[value.category]}
                                            </Lozenge>
                                        </div>
                                        {value.parentName &&
                                            <div className="muted-text">
                                                {value.parentName}
                                            </div>
                                        }
                                        <div className={value.deleted ? 'crossed-text' : ''}>
                                            {value.scriptName}
                                        </div>
                                    </div>
                                )
                            },
                            {
                                content: value.description
                            },
                            {
                                content: value.deleted && (value.action === 'DELETED') && !!value.scriptId && (
                                    <Tooltip content={AuditMessages.restore}>
                                        <Button
                                            iconBefore={<UndoIcon label="Undo"/>}

                                            onClick={this._restore(value.category, value.scriptId)}
                                        />
                                    </Tooltip>
                                )
                            }
                        ]
                    };
                }),
                isReady: true
            }));
    }

    _goToPage = (page: number) => this._loadList(this.state.data.limit * (page-1), this.state.filter);

    _updateFilter = (filter: AuditLogFilterType) => {
        this.setState({ filter }, () => this._loadList(0, filter));
    };

    componentDidMount() {
        this._loadList(0, this.state.filter);
    }

    _renderPagination(): Element<typeof Pagination> {
        const {data} = this.state;

        return (
            <Pagination
                value={(data.offset/data.limit + 1) || 0}
                total={Math.ceil(data.total/data.limit) || 0}
                onChange={this._goToPage}

                i18n={{
                    prev: CommonMessages.prev,
                    next: CommonMessages.next
                }}
            />
        );
    }

    render() {
        const {isReady, rows, filter} = this.state;

        return <Page>
            <PageHeader
                bottomBar={
                    <div className="flex-row">
                        <AuditLogFilter value={filter} onChange={this._updateFilter}/>
                    </div>
                }

                breadcrumbs={<Breadcrumbs>{withRoot([])}</Breadcrumbs>}
            >
                {TitleMessages.audit}
            </PageHeader>

            <div className="page-content">
                {this._renderPagination()}
                <DynamicTableStateless
                    head={tableHead}
                    emptyView={<InfoMessage title={AuditMessages.noItems}/>}

                    isLoading={!isReady}
                    rows={rows}
                />
                {this._renderPagination()}
            </div>
        </Page>;
    }
}

