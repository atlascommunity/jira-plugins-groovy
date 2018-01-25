import React from 'react';
import PropTypes from 'prop-types';

import Button from 'aui-react/lib/AUIButton';

import {AuditLogEntryModel} from '../model/audit.model';
import {auditLogService} from '../service/services';
import {CommonMessages, FieldMessages, TitleMessages} from '../i18n/common.i18n';
import {AuditMessages} from '../i18n/audit.i18n';


export class AuditLogContainer extends React.Component {
    state = {
        offset: 0,
        isReady: false,
        data: null
    };

    _loadList(offset) {
        this.setState({
            isReady: false
        });

        auditLogService
            .getAuditLogPage(offset)
            .then(data => this.setState({ data, offset, isReady: true }));
    }

    _goToOffset = (offset) => () => this._loadList(offset);

    componentDidMount() {
        this._loadList(0);
    }

    render() {
        const {isReady, data} = this.state;

        if (!isReady) {
            return <div className="spinner"/>;
        }

        return <div>
            <header className="aui-page-header">
                <div className="aui-page-header-inner">
                    <div className="aui-page-header-main">
                        <h2>{TitleMessages.audit}</h2>
                    </div>
                </div>
            </header>

            <div className="page-content">
                <div>
                    <strong>
                        {data.offset+1}{'-'}{data.offset+data.size}
                    </strong>
                    {' '}{CommonMessages.of}{' '}
                    <strong>
                        {data.total}
                        </strong>
                </div>
                <AuditLog entries={data.values}/>
                <div>
                    <Button
                        type="link"
                        disabled={data.offset === 0}
                        onClick={this._goToOffset(data.offset - data.limit)}
                    >
                        {CommonMessages.prev}
                    </Button>
                    <Button
                        type="link"
                        disabled={data.isLast}
                        onClick={this._goToOffset(data.offset + data.limit)}
                    >
                        {CommonMessages.next}
                    </Button>
                </div>
            </div>
        </div>;
    }
}

class AuditLog extends React.Component {
    static propTypes = {
        entries: PropTypes.arrayOf(AuditLogEntryModel).isRequired
    };

    render() {
        const {entries} = this.props;

        return <table className="aui">
            <thead>
                <tr>
                    <th>{'#'}</th>
                    <th>{FieldMessages.date}</th>
                    <th>{AuditMessages.user}</th>
                    <th>{AuditMessages.category}</th>
                    <th>{AuditMessages.action}</th>
                    <th>{AuditMessages.description}</th>
                </tr>
            </thead>
            <tbody>
                {entries.map(entry =>
                    <tr key={entry.id}>
                        <td>
                            {entry.id}
                            </td>
                        <td>
                            {entry.date}
                        </td>
                        <td>
                            <span className="aui-avatar aui-avatar-xsmall">
                                <span className="aui-avatar-inner">
                                    <img src={entry.user.avatarUrl} alt=""/>
                                </span>
                            </span>
                            {' '}
                            {entry.user.displayName}
                        </td>
                        <td>
                            {entry.category}
                        </td>
                        <td>
                            {entry.action}
                        </td>
                        <td>
                            {entry.description}
                        </td>
                    </tr>
                )}
            </tbody>
        </table>;
    }
}
