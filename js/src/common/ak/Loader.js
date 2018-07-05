//@flow
import React, {Fragment, type Element, type ComponentType} from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import memoizeOne from 'memoize-one';

import {LoadingSpinner} from './LoadingSpinner';


type Props = {
    isReady: boolean,
    children: Element<any>
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

export const Loader: ComponentType<{children: Element<any>}> = withRouter(connect(
    memoizeOne(
        ({isReady}): {isReady: boolean} => ({isReady}),
    )
)(LoaderInternal));
