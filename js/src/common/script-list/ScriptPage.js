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
    DialogComponent?: React.ComponentType<FullDialogComponentProps>,

    isCreateDisabled: boolean
};

type State = {
    editProps: ?DialogComponentProps
};

export class ScriptPage<T> extends React.PureComponent<Props<T&ItemType>, State> {
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

    render(): React.Node {
        const {isReady, items, i18n, DialogComponent, ScriptComponent, isCreateDisabled} = this.props;
        const {editProps} = this.state;

        return (
            <Page>
                <PageHeader
                    actions={
                        !isCreateDisabled ? <Button appearance="primary" onClick={this._triggerCreate} isDisabled={!isReady}>
                            {i18n.addItem}
                        </Button> : undefined
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

                {editProps && DialogComponent ? <DialogComponent {...editProps} onClose={this._closeEdit}/> : null}
            </Page>
        );
    }
}
