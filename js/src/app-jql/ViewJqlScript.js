//@flow
import React from 'react';

import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import {JqlScript} from './JqlScript';
import type {JqlScriptType} from './types';

import { deleteItem } from '../common/redux';

import {RouterLink} from '../common/ak/RouterLink';
import {RegistryMessages} from '../i18n/registry.i18n';
import {CommonMessages} from '../i18n/common.i18n';
import {jqlScriptService} from '../service';
import {DeleteDialog} from '../common/script-list/DeleteDialog';
import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {withRoot} from '../common/script-list/breadcrumbs';
import {createItemSelector} from '../common/redux/selectors';


type Props = {
    id: number,
    script?: JqlScriptType,
    deleteItem: typeof deleteItem,
    history: any
};

type State = {
    isDeleting: boolean
};

class ViewJqlScriptInternal extends React.PureComponent<Props, State> {
    state = {
        isDeleting: false
    };

    _toggleDelete = () => this.setState(state => ({ isDeleting: !state.isDeleting }));

    _doDelete = () => jqlScriptService
        .deleteScript(this.props.id)
        .then(() => this.props.history.push('/jql/'));

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
                                    text="JQL scripts"
                                    href="/jql/"

                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {script ? script.name : 'Unknown script'}
                </PageHeader>
                {script ?
                    <JqlScript script={script} collapsible={false} onDelete={this._toggleDelete}/> :
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

export const ViewJqlScript = withRouter(
    connect(
        (): * => {
            const itemSelector = createItemSelector();
            //$FlowFixMe
            return (state, props) => ({
                script: itemSelector(state, props)
            });
        },
        { deleteItem }
    )(ViewJqlScriptInternal)
);
