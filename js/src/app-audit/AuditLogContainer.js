import React from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Tooltip from '@atlaskit/tooltip';
import Avatar, {AvatarItem} from '@atlaskit/avatar';
import Lozenge from '@atlaskit/lozenge';
import {DynamicTableStateless} from '@atlaskit/dynamic-table';
import {PaginationStateless} from '@atlaskit/pagination';

import QuestionIcon from '@atlaskit/icon/glyph/question';
import AddCircleIcon from '@atlaskit/icon/glyph/add-circle';
import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';
import TrashIcon from '@atlaskit/icon/glyph/trash';
import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';
import CrossCircleIcon from '@atlaskit/icon/glyph/cross-circle';
import ArrowRightCircleIcon from '@atlaskit/icon/glyph/arrow-right-circle';

import {auditLogService} from '../service/services';
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
            width: '200px'
        },
        {
            content: AuditMessages.description
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

export class AuditLogContainer extends React.Component {
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
                                        <div>
                                            {value.scriptName}
                                        </div>
                                    </div>
                                )
                            },
                            {
                                content: value.description
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

