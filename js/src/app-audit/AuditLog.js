import React from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Tooltip from '@atlaskit/tooltip';
import Avatar, {AvatarItem} from '@atlaskit/avatar';
import Lozenge from '@atlaskit/lozenge';
import Button from '@atlaskit/button';
import {DynamicTableStateless} from '@atlaskit/dynamic-table';
import {PaginationStateless} from '@atlaskit/pagination';

import QuestionIcon from '@atlaskit/icon/glyph/question';
import AddCircleIcon from '@atlaskit/icon/glyph/add-circle';
import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';
import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';
import CrossCircleIcon from '@atlaskit/icon/glyph/cross-circle';
import ArrowRightCircleIcon from '@atlaskit/icon/glyph/arrow-right-circle';
import UndoIcon from '@atlaskit/icon/glyph/undo';

import {
    auditLogService,
    listenerService,
    registryService,
    restService,
    scheduledTaskService
} from '../service/services';
import {CommonMessages, FieldMessages, TitleMessages} from '../i18n/common.i18n';
import {AuditMessages, CategoryNameMessages} from '../i18n/audit.i18n';


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

function ActionsIcon({action}) {
    let icon = null;
    switch (action) {
        case 'CREATED':
            icon = <AddCircleIcon label={action}/>;
            break;
        case 'UPDATED':
            icon = <EditFilledIcon label={action}/>;
            break;
        case 'DELETED':
            icon = <TrashIcon label={action}/>;
            break;
        case 'ENABLED':
            icon = <CheckCircleIcon label={action}/>;
            break;
        case 'DISABLED':
            icon = <CrossCircleIcon label={action}/>;
            break;
        case 'MOVED':
            icon = <ArrowRightCircleIcon label={action}/>;
            break;
        case 'RESTORED':
            icon = <UndoIcon label={action}/>;
            break;
        default:
            icon = <QuestionIcon label={action}/>;
            break;
    }

    return (
        <Tooltip content={action}>
            {icon}
        </Tooltip>
    );
}

export class AuditLog extends React.Component {
    state = {
        offset: 0,
        isReady: false,
        rows: [],
        data: {
            offset: 0,
            limit: 1,
            values: []
        }
    };

    _restore = (category, id) => () => {
        let promise = null;
        switch (category) {
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
        }

        if (promise) {
            this.setState({ isReady: false });

            promise
                .then(
                    () => this._loadList(this.state.offset),
                    error => {
                        this.setState({ isReady: true });
                        throw error;
                    }
                );
        }
    };

    _loadList(offset) {
        this.setState({
            isReady: false
        });

        auditLogService
            .getAuditLogPage(offset)
            .then(data => this.setState({
                data,
                rows: data.values.map(value => {
                    return {
                        key: value.id,
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
                                content: <ActionsIcon action={value.action}/>
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
                                content: value.deleted && (value.action === 'DELETED') && (
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

    _goToPage = (page) => this._loadList(this.state.data.limit * (page-1));

    componentDidMount() {
        this._loadList(0);
    }

    _renderPagination() {
        const {data} = this.state;

        return (
            <PaginationStateless
                current={(data.offset/data.limit + 1) || 0}
                total={Math.ceil(data.total/data.limit) || 0}
                onSetPage={this._goToPage}

                i18n={{
                    prev: CommonMessages.prev,
                    next: CommonMessages.next
                }}
            />
        );
    }

    render() {
        const {isReady, rows} = this.state;

        return <Page>
            <PageHeader>
                {TitleMessages.audit}
            </PageHeader>

            <div className="page-content">
                {this._renderPagination()}
                <DynamicTableStateless
                    head={tableHead}
                    isLoading={!isReady}
                    rows={rows}
                />
                {this._renderPagination()}
            </div>
        </Page>;
    }
}

