//@flow
import * as React from 'react';

import {connect} from 'react-redux';
import memoizeOne from 'memoize-one';

import {ListenerRegistry} from './ListenerRegistry';
import {ListenerDialog} from './ListenerDialog';

import type {ListenerType} from './types';

import {LoadingSpinner} from '../common/ak/LoadingSpinner';


type Props = {
    listeners: Array<ListenerType>,
    ready: boolean
};

type State = {
    dialogProps: ?{
        isNew: boolean,
        id: ?number
    }
};

class ListenerRegistryContainerInternal extends React.PureComponent<Props, State> {
    state = {
        dialogProps: null
    };

    _triggerDialog = (isNew, id) => this.setState({ dialogProps: {isNew, id} });

    _closeDialog = () => this.setState({ dialogProps: null });

    render(): React.Node {
        const {dialogProps} = this.state;
        const {listeners, ready} = this.props;

        let content: React.Node = null;

        if (!ready) {
            content = <LoadingSpinner/>;
        } else {
            content = <ListenerRegistry listeners={listeners} triggerDialog={this._triggerDialog}/>;
        }

        return (
            <div>
                {content}
                {dialogProps && <ListenerDialog {...dialogProps} onClose={this._closeDialog}/>}
            </div>
        );
    }
}

export const ListenerRegistryContainer = connect(
    memoizeOne(
        (state: *): * => {
            return {
                listeners: state.items,
                ready: state.ready
            };
        }
    )
)(ListenerRegistryContainerInternal);
