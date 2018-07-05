//@flow
import React, {type ComponentType, type Element} from 'react';

import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs from '@atlaskit/breadcrumbs';

import {ScriptList} from './ScriptList';

import type {ScriptComponentProps, I18nType, BreadcrumbsType} from './types';

import type {DeleteDialogProps} from './DeleteDialog';
import {ConnectedDeleteDialog} from './DeleteDialog';

import type {ItemType} from '../redux';


type Props<T> = {|
    i18n: I18nType,
    isReady: boolean,
    items: Array<T>,

    ScriptComponent: ComponentType<ScriptComponentProps<T>>,
    breadcrumbs?: BreadcrumbsType,

    actions?: Element<any>,
|};

type State = {
    deleteProps: ?DeleteDialogProps
};

export class ScriptPage<T: ItemType> extends React.PureComponent<Props<T>, State> {
    state = {
        editProps: null,
        deleteProps: null
    };

    _triggerDelete = (id: number, name: string, onConfirm: () => Promise<void>) => this.setState({ deleteProps: {id, name, onConfirm} });

    _closeDelete = () => this.setState({ deleteProps: null });

    render() {
        const {isReady, items, i18n, ScriptComponent, actions, breadcrumbs} = this.props;
        const {deleteProps} = this.state;

        return (
            <Page>
                <PageHeader
                    actions={actions}
                    breadcrumbs={breadcrumbs && <Breadcrumbs>{breadcrumbs}</Breadcrumbs>}
                >
                    {i18n.title}
                </PageHeader>

                <div className="page-content">
                    <ScriptList
                        isReady={isReady}
                        items={items}

                        i18n={i18n}
                        ScriptComponent={ScriptComponent}
                        onDelete={this._triggerDelete}
                    />
                </div>

                {deleteProps && <ConnectedDeleteDialog {...deleteProps} i18n={i18n.delete} onClose={this._closeDelete}/>}
            </Page>
        );
    }
}
