//@flow
import React from 'react';


export class ScrollToTop extends React.PureComponent<{}> {
    componentDidMount() {
        window.scrollTo(0, 0);
    }

    render() {
        return null;
    }
}
