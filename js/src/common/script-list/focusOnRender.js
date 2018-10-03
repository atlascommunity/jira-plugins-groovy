//@flow
import React, {type ComponentType} from 'react';
import ReactDOM from 'react-dom';

import {withRouter, type RouterHistory, type Match, type Location} from 'react-router-dom';

import type {ScriptComponentProps} from './types';

import type {ItemType} from '../redux';


type Props = {
    history: RouterHistory,
    location: Location,
    match: Match
};

export function focusOnRender<T: ItemType>(WrappedComponent: ComponentType<ScriptComponentProps<T>>): ComponentType<ScriptComponentProps<T>> {
    class FocusOnRenderComponent extends React.PureComponent<ScriptComponentProps<T> & Props, {focused: boolean}> {
        state = {
            focused: false
        };

        componentDidMount() {
            const {location, script} = this.props;

            if (location.state && script && location.state.focus === script.id) {
                this.setState({ focused: true }, () => {
                    const domNode = ReactDOM.findDOMNode(this);
                    if (domNode instanceof Element) {
                        domNode.scrollIntoView(true);
                        window.scrollBy(0, -50);
                    } else {
                        console.warn('domNode is not element', domNode);
                    }
                });
            }
        }

        render() {
            const {history, location, match, ...props} = this.props;

            return <WrappedComponent {...props} focused={this.state.focused}/>;
        }
    }

    return withRouter(FocusOnRenderComponent);
}
