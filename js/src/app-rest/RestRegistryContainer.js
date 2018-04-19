import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import {RestRegistry} from './RestRegistry';
import {RestScriptDialog} from './RestScriptDialog';

import {LoadingSpinner} from '../common/ak/LoadingSpinner';


@connect(
    state => {
        return {
            scripts: state.scripts,
            ready: state.ready
        };
    }
)
export class RestRegistryContainer extends React.Component {
    static propTypes ={
        scripts: PropTypes.arrayOf(PropTypes.object).isRequired, //todo: shape
        ready: PropTypes.bool.isRequired
    };

    state = {
        dialogProps: null
    };

    _triggerDialog = (isNew, id) => this.setState({ dialogProps: {isNew, id} });

    _closeDialog = () => this.setState({ dialogProps: null });

    render() {
        const {dialogProps} = this.state;
        const {scripts, ready} = this.props;

        let content = null;

        if (!ready) {
            content = <LoadingSpinner/>;
        } else {
            content = <RestRegistry scripts={scripts} triggerDialog={this._triggerDialog}/>;
        }

        return (
            <div>
                {content}
                {dialogProps && <RestScriptDialog {...dialogProps} onClose={this._closeDialog}/>}
            </div>
        );
    }
}
