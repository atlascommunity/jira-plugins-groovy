//@flow
import React, {Fragment} from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import memoizeOne from 'memoize-one';

import {LoadingSpinner} from './LoadingSpinner';


type Props = {
    isReady: boolean,
    children: Node
};

export class LoaderInternal extends React.PureComponent<Props> {
    render() {
        const {isReady, children} = this.props;

        if (isReady) {
            return <Fragment>{children}</Fragment>;
        }

        return <LoadingSpinner/>;
    }
}

export const Loader = withRouter(connect(memoizeOne(({isReady}) => ({isReady})))(LoaderInternal));
