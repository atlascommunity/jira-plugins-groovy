import React from 'react';
import PropTypes from 'prop-types';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';


export class Tooltipped extends React.Component {
    static PropTypes = {
        title: PropTypes.string.isRequired,
        children: PropTypes.any.isRequired
    };

    el = null;

    _setEl = (el) => this.el = el;

    componentDidMount() {
        AJS.$(this.el).tooltip({gravity: 'n'});
    }

    render() {
        return <div ref={this._setEl} title={this.props.title}>{this.props.children}</div>;
    }
}
