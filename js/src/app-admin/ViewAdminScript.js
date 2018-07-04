//@flow
import React from 'react';

import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import {AdminScript} from './AdminScript';
import type {AdminScriptType} from './types';

import { deleteItem } from '../common/redux';

import {RouterLink} from '../common/ak/RouterLink';
import {RegistryMessages} from '../i18n/registry.i18n';
import {CommonMessages} from '../i18n/common.i18n';
import {adminScriptService} from '../service/services';
import {DeleteDialog} from '../common/script-list/DeleteDialog';
import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {withRoot} from '../common/script-list/breadcrumbs';
import {createItemSelector} from '../common/redux/selectors';


type Props = {
    id: number,
    script?: AdminScriptType,
    deleteItem: typeof deleteItem,
    history: any
};

type State = {
    isDeleting: boolean
};

class ViewAdminScriptInternal extends React.PureComponent<Props, State> {
    state = {
        isDeleting: false
    };

    _toggleDelete = () => this.setState(state => ({ isDeleting: !state.isDeleting }));

    _doDelete = () => adminScriptService
        .deleteScript(this.props.id)
        .then(() => this.props.history.push('/admin-scripts/'));

    render() {
        const {script, deleteItem} = this.props;
        const {isDeleting} = this.state;

        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="registry"
                                    text="Admin scripts"
                                    href="/admin-scripts/"

                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {script ? script.name : 'Unknown script'}
                </PageHeader>
                {script ?
                    <AdminScript script={script} collapsible={false} onDelete={this._toggleDelete}/> :
                    <NotFoundPage/>
                }
                {isDeleting && script &&
                    <DeleteDialog
                        id={script.id}
                        name={script.name}

                        deleteItem={deleteItem}
                        onConfirm={this._doDelete}
                        onClose={this._toggleDelete}

                        i18n={{
                            heading: RegistryMessages.deleteScript,
                            areYouSure: CommonMessages.confirmDelete
                        }}
                    />
                }
            </Page>
        );
    }
}

export const ViewAdminScript = withRouter(
    connect(
        (): * => {
            const itemSelector = createItemSelector();
            //$FlowFixMe
            return (state, props) => ({
                script: itemSelector(state, props)
            });
        },
        { deleteItem }
    )(ViewAdminScriptInternal)
);
