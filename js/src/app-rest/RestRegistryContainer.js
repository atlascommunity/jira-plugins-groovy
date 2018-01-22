import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {RestRegistry} from './RestRegistry';
import {RestScriptDialog} from './RestScriptDialog';

import {CommonMessages} from '../i18n/common.i18n';


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

    componentDidUpdate(prevProps) {
        if (this.props.ready !== prevProps.ready) {
            if (this.props.ready) {
                AJS.undim();
            } else {
                AJS.dim();
            }
        }
    }

    render() {
        const {dialogProps} = this.state;
        const {scripts, ready} = this.props;

        let content = null;

        if (!ready) {
            console.log('loading');
            content = <div>{CommonMessages.loading}</div>;
        } else {
            console.log(scripts);
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
