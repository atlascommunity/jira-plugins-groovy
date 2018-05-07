//@flow
import * as React from 'react';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {ScriptList} from './ScriptList';

import type {FullDialogComponentProps, DialogComponentProps, ScriptComponentProps, I18nType} from './types';

import type {ItemType} from '../redux';


type Props<T> = {
    i18n: I18nType,
    isReady: boolean,
    items: Array<T>,
    ScriptComponent: React.ComponentType<ScriptComponentProps<T>>,
    DialogComponent: React.ComponentType<FullDialogComponentProps>
};

type State = {
    editProps: ?DialogComponentProps
};

export class ScriptPage<T> extends React.PureComponent<Props<T&ItemType>, State> {
    state = {
        editProps: null,
        deleteProps: null
    };

    _triggerCreate = () => this.setState({ editProps: {isNew: true, id: null} });

    _triggerEdit = (id: number) => this.setState({ editProps: {isNew: false, id} });

    _closeEdit = () => this.setState({ editProps: null });

    render(): React.Node {
        const {isReady, items, i18n, DialogComponent, ScriptComponent} = this.props;
        const {editProps} = this.state;

        return (
            <Page>
                <PageHeader
                    actions={
                        <Button appearance="primary" onClick={this._triggerCreate} isDisabled={!isReady}>
                            {i18n.addItem}
                        </Button>
                    }
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
                    />
                </div>

                {editProps ? <DialogComponent {...editProps} onClose={this._closeEdit}/> : null}
            </Page>
        );
    }
}
