//@flow
import React, {Fragment} from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import memoizeOne from 'memoize-one';

import {LoadingSpinner} from '../common/ak/LoadingSpinner';


type Props = {
    ready: boolean,
    children: Node
};

export class LoaderInternal extends React.PureComponent<Props> {
    render() {
        const {ready, children} = this.props;

        if (ready) {
            return <Fragment>{children}</Fragment>;
        }

        return <LoadingSpinner/>;
    }
}

export const Loader = withRouter(connect(memoizeOne(({ready}) => ({ready})))(LoaderInternal));
