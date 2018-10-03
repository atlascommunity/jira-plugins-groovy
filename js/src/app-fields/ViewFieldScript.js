//@flow
import React from 'react';

import {withRouter, type RouterHistory} from 'react-router-dom';
import {connect} from 'react-redux';

import Breadcrumbs, {BreadcrumbsItem} from '@atlaskit/breadcrumbs';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {FieldScript} from './FieldScript';
import type {FieldConfigItem} from './types';

import {withRoot} from '../common/script-list/breadcrumbs';
import {NotFoundPage} from '../common/script-list/NotFoundPage';

import {createItemSelector} from '../common/redux/selectors';
import {RouterLink} from '../common/ak/RouterLink';


type Props = {
    id: number,
    script?: FieldConfigItem,
    history: RouterHistory
};

class ViewFieldScriptInternal extends React.PureComponent<Props> {
    render() {
        const {script: fieldConfig} = this.props;

        return (
            <Page>
                <PageHeader
                    breadcrumbs={
                        <Breadcrumbs>
                            {withRoot([
                                <BreadcrumbsItem
                                    key="registry"
                                    text="Scripted fields"
                                    href="/fields/"
                                    component={RouterLink}
                                />
                            ])}
                        </Breadcrumbs>
                    }
                >
                    {fieldConfig ? `${fieldConfig.customFieldName} - ${fieldConfig.contextName}` : 'Unknown script'}
                </PageHeader>
                {fieldConfig
                    ? <FieldScript script={fieldConfig} collapsible={false}/>
                    : <NotFoundPage/>
                }
            </Page>
        );
    }
}

export const ViewFieldScript = withRouter(
    connect(
        (): * => {
            const itemSelector = createItemSelector();
            //$FlowFixMe
            return (state, props) => ({
                script: itemSelector(state, props)
            });
        }
    )(ViewFieldScriptInternal)
);
