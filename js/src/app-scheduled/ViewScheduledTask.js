//@flow
import React from 'react';

import {withRouter} from 'react-router-dom';
import {connect} from 'react-redux';

import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {ScheduledTask} from './ScheduledTask';
import type {ScheduledTaskType} from './types';

import {withRoot} from '../common/script-list/breadcrumbs';
import {NotFoundPage} from '../common/script-list/NotFoundPage';

import {createItemSelector} from '../common/redux/selectors';
import {RouterLink} from '../common/ak/RouterLink';


type Props = {
    id: number,
    script?: ScheduledTaskType,
    history: any
};

class ViewScheduledTaskInternal extends React.PureComponent<Props> {
    render() {
        const {script} = this.props;

        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="scheduled"
                                    text="Scheduled tasks"
                                    href="/scheduled/"

                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {script ? script.name : 'Unknown script'}
                </PageHeader>
                {script ?
                    <ScheduledTask script={script} collapsible={false}/> :
                    <NotFoundPage/>
                }
            </Page>
        );
    }
}

export const ViewScheduledTask = withRouter(
    connect(
        (): * => {
            const itemSelector = createItemSelector();
            //$FlowFixMe
            return (state, props) => ({
                script: itemSelector(state, props)
            });
        }
    )(ViewScheduledTaskInternal)
);
