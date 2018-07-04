//@flow
import React from 'react';

import {withRouter} from 'react-router-dom';
import {connect} from 'react-redux';

import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {Listener} from './Listener';
import type {ListenerType} from './types';

import {withRoot} from '../common/script-list/breadcrumbs';
import {NotFoundPage} from '../common/script-list/NotFoundPage';

import {createItemSelector} from '../common/redux/selectors';
import {RouterLink} from '../common/ak/RouterLink';


type Props = {
    id: number,
    script?: ListenerType,
    history: any
};

class ViewListenerInternal extends React.PureComponent<Props> {
    render() {
        const {script} = this.props;

        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="listeners"
                                    text="Listeners"
                                    href="/listeners/"

                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {script ? script.name : 'Unknown script'}
                </PageHeader>
                {script ?
                    <Listener script={script} collapsible={false}/> :
                    <NotFoundPage/>
                }
            </Page>
        );
    }
}

export const ViewListener = withRouter(
    connect(
        (): * => {
            const itemSelector = createItemSelector();
            //$FlowFixMe
            return (state, props) => ({
                script: itemSelector(state, props)
            });
        }
    )(ViewListenerInternal)
);
