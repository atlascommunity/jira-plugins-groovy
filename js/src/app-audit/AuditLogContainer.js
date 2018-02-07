import React from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import {DynamicTableStateless} from '@atlaskit/dynamic-table';
import {PaginationStateless} from '@atlaskit/pagination';

import {auditLogService} from '../service/services';
import {CommonMessages, FieldMessages, TitleMessages} from '../i18n/common.i18n';
import {AuditMessages} from '../i18n/audit.i18n';


const tableHead = {
    cells: [
        {
            content: '#'
        },
        {
            content: FieldMessages.date
        }, {
            content: AuditMessages.user
        }, {
            content: AuditMessages.category
        }, {
            content: AuditMessages.action
        }, {
            content: AuditMessages.description
        }
    ]
};


//todo: migrate to Dynamic table: https://ak-mk-2-prod.netlify.com/mk-2/packages/elements/dynamic-table
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
                                content: value.id
                            },
                            {
                                content: value.date
                            }, {
                                content: (
                                    <div>
                                        <span className="aui-avatar aui-avatar-xsmall">
                                            <span className="aui-avatar-inner">
                                                <img src={value.user.avatarUrl} alt=""/>
                                            </span>
                                        </span>
                                        {' '}
                                        {value.user.displayName}
                                    </div>
                                )
                            }, {
                                content: value.category
                            }, {
                                content: value.action
                            }, {
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

