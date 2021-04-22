//@flow
import React from 'react';

import {connect} from 'react-redux';
import {withRouter, type RouterHistory} from 'react-router-dom';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';

import { deleteScript } from './redux';
import type {RegistryScriptType} from './types';
import {RegistryScript} from './RegistryScript';

import {RouterLink} from '../common/ak/RouterLink';
import {RegistryMessages} from '../i18n/registry.i18n';
import {CommonMessages} from '../i18n/common.i18n';
import {registryService} from '../service';
import {DeleteDialog} from '../common/script-list/DeleteDialog';
import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {withRoot} from '../common/script-list/breadcrumbs';
import { LoadingSpinner } from '../common/ak';


type Props = {
    id: number,
    history: RouterHistory
};

type State = {
    isDeleting: boolean,
    isLoading: boolean,
    script?: RegistryScriptType
};

class ViewScriptInternal extends React.PureComponent<Props, State> {
    state = {
        isDeleting: false,
        script: undefined,
    };

    _toggleDelete = () => this.setState(state => ({ isDeleting: !state.isDeleting }));

    _doDelete = () => registryService
        .deleteScript(this.props.id)
        .then(() => this.props.history.push('/registry/'));

    componentDidMount() {
      registryService
        .getScript(this.props.id)
        .then((script) => this.setState(state => ({...state, script: script })))
    }

    render() {
        const {isDeleting, script} = this.state;

        if (!script) {
          return <LoadingSpinner/>;
        }
        if (script.deleted) {
          return <NotFoundPage/>;
        }
        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="registry"
                                    text="Workflow script registry"
                                    href="/registry/"
                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {script ? script.name : 'Unknown script'}
                </PageHeader>
                {script
                    ? <RegistryScript script={script} collapsible={false} onDelete={this._toggleDelete} showParent={true}/>
                    : <NotFoundPage/>
                }
                {isDeleting && script &&
                    <DeleteDialog
                        id={script.id}
                        name={script.name}
                        deleteItem={this.props.deleteScript}
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

export const ViewScript = withRouter(
    connect(
      null,
      { deleteScript }
    )(ViewScriptInternal)
);
