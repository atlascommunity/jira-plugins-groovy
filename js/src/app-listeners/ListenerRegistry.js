//@flow
import * as React from 'react';
import PropTypes from 'prop-types';

import Button from '@atlaskit/button';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';

import {Listener} from './Listener';

import type {ListenerType} from './types';

import {ListenerModel} from '../model/listener.model';

import {ListenerMessages} from '../i18n/listener.i18n';
import {TitleMessages} from '../i18n/common.i18n';

import {InfoMessage} from '../common/ak/messages';

import './ListenerRegistry.less';


type Props = {
    listeners: Array<ListenerType>,
    triggerDialog: (isNew: boolean, id: ?number) => void
};

export class ListenerRegistry extends React.PureComponent<Props> {
    static propTypes = {
        listeners: PropTypes.arrayOf(ListenerModel).isRequired,
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew: boolean, id: ?number) => () => this.props.triggerDialog(isNew, id);

    render(): React.Node {
        const {listeners} = this.props;

        return (
            <Page>
                <PageHeader
                    actions={
                        <Button
                            appearance="primary"
                            onClick={this._triggerDialog(true)}
                        >
                            {ListenerMessages.addListener}
                        </Button>
                    }
                >
                    {TitleMessages.listeners}
                </PageHeader>

                <div className="page-content ScriptList">
                    {listeners.map(listener =>
                        //$FlowFixMe wtf
                        <Listener
                            key={listener.id}
                            listener={listener}
                            onEdit={this._triggerDialog(false, listener.id)}
                        />
                    )}
                    {!listeners.length && <InfoMessage title={ListenerMessages.noListeners}/>}
                </div>
            </Page>
        );
    }
}
