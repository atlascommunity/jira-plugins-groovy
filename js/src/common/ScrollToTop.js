//@flow
import React from 'react';

import {withRouter, type Location} from 'react-router-dom';


class ScrollToTopInternal extends React.PureComponent<{location: Location}> {
    componentDidMount() {
        const {location} = this.props;

        if (!(location.state && location.state.focus)) {
            window.scrollTo(0, 0);
            // eslint-disable-next-line no-console
            console.log('scrolling to top');
        }
    }

    render() {
        return null;
    }
}

export const ScrollToTop = withRouter(ScrollToTopInternal);
