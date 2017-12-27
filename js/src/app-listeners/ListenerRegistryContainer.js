import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ListenerRegistry} from './ListenerRegistry';
import {ListenerDialog} from './ListenerDialog';

import {CommonMessages} from '../i18n/common.i18n';
import {listenerService} from '../service/services';


export class ListenerRegistryContainer extends React.Component {
    state = {
        ready: true,
        listeners: [],
        dialogProps: null
    };

    componentDidUpdate(prevState) {
        const readyNow = this.state.ready;
        if (readyNow !== prevState.ready) {
            if (readyNow) {
                AJS.undim();
            } else {
                AJS.dim();
            }
        }
    }

    componentDidMount() {
        this.setState({ready: false});

        listenerService
            .getAllListeners()
            .then(listeners => this.setState({ listeners, ready: true }));
    }

    _triggerDialog = (isNew, id) => this.setState({ dialogProps: {isNew, id} });

    _closeDialog = () => this.setState({ dialogProps: null });

    render() {
        const {ready, dialogProps} = this.state;

        let content = null;

        if (!ready) {
            content = <div>{CommonMessages.loading}</div>;
        } else {
            content = <ListenerRegistry listeners={this.state.listeners} triggerDialog={this._triggerDialog}/>;
        }

        return (
            <div>
                {content}
                {dialogProps && <ListenerDialog {...dialogProps} onClose={this._closeDialog}/>}
            </div>
        );
    }
}
