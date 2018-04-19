import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {ListenerRegistry} from './ListenerRegistry';
import {ListenerDialog} from './ListenerDialog';

import {ListenerModel} from '../model/listener.model';
import {LoadingSpinner} from '../common/ak/LoadingSpinner';


@connect(
    state => {
        return {
            listeners: state.listeners,
            ready: state.ready
        };
    }
)
export class ListenerRegistryContainer extends React.Component {
    static propTypes ={
        listeners: PropTypes.arrayOf(ListenerModel).isRequired,
        ready: PropTypes.shape({
            listeners: PropTypes.bool.isRequired,
            projects: PropTypes.bool.isRequired,
            events: PropTypes.bool.isRequired
        }).isRequired
    };

    state = {
        dialogProps: null
    };

    _isReady(props=this.props) {
        const ready = props.ready;
        return ready.listeners && ready.projects && ready.events;
    }

    _triggerDialog = (isNew, id) => this.setState({ dialogProps: {isNew, id} });

    _closeDialog = () => this.setState({ dialogProps: null });

    componentDidUpdate(prevProps) {
        const readyNow = this._isReady();
        if (readyNow !== this._isReady(prevProps)) {
            if (readyNow) {
                AJS.undim();
            } else {
                AJS.dim();
            }
        }
    }

    render() {
        const {dialogProps} = this.state;
        const {listeners} = this.props;

        let content = null;

        if (!this._isReady()) {
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
