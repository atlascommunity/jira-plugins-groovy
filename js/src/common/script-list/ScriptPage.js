//@flow
import React, {type ComponentType, type Element} from 'react';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import Breadcrumbs from '@atlaskit/breadcrumbs';

import {ScriptList} from './ScriptList';

import type {FullDialogComponentProps, DialogComponentProps, ScriptComponentProps, I18nType, BreadcrumbsType} from './types';

import type {DeleteDialogProps} from './DeleteDialog';
import {ConnectedDeleteDialog} from './DeleteDialog';

import type {ItemType} from '../redux';


type Props<T> = {
    i18n: I18nType,
    isReady: boolean,
    items: Array<T>,

    ScriptComponent: ComponentType<ScriptComponentProps<T>>,
    DialogComponent?: ComponentType<FullDialogComponentProps>,
    breadcrumbs?: BreadcrumbsType,

    isCreateDisabled: boolean,

    actions?: Element<any>,
};

type State = {
    editProps: ?DialogComponentProps,
    deleteProps: ?DeleteDialogProps
};

export class ScriptPage<T: ItemType> extends React.PureComponent<Props<T>, State> {
    static defaultProps = {
        isCreateDisabled: false
    };

    state = {
        editProps: null,
        deleteProps: null
    };

    _triggerCreate = () => this.setState({ editProps: {isNew: true, id: null} });

    _triggerEdit = (id: number) => this.setState({ editProps: {isNew: false, id} });

    _closeEdit = () => this.setState({ editProps: null });

    _triggerDelete = (id: number, name: string, onConfirm: () => Promise<void>) => this.setState({ deleteProps: {id, name, onConfirm} });

    _closeDelete = () => this.setState({ deleteProps: null });

    render() {
        const {isReady, items, i18n, DialogComponent, ScriptComponent, isCreateDisabled, actions, breadcrumbs} = this.props;
        const {editProps, deleteProps} = this.state;

        return (
            <Page>
                <PageHeader
                    actions={
                        actions || (
                            !isCreateDisabled ?
                                <Button appearance="primary" onClick={this._triggerCreate} isDisabled={!isReady}>
                                    {i18n.addItem}
                                </Button>:
                                undefined
                        )
                    }
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
                        onEdit={this._triggerEdit}
                        onDelete={this._triggerDelete}
                    />
                </div>

                {editProps && DialogComponent ? <DialogComponent {...editProps} onClose={this._closeEdit}/> : null}
                {deleteProps && <ConnectedDeleteDialog {...deleteProps} i18n={i18n.delete} onClose={this._closeDelete}/>}
            </Page>
        );
    }
}
