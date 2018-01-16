import React from 'react';
import PropTypes from 'prop-types';

import 'brace';

import 'brace/mode/groovy';
import 'brace/mode/diff';
import 'brace/theme/chrome';

import Ace from 'react-ace';

import './Editor.less';


let count = 0;

export class Editor extends React.Component {
    static propTypes = {
        mode: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired
    };

    componentDidMount() {
        this.i = count++;
    }

    render() {
        return <Ace
            theme="chrome"
            name={`ace-editor-${this.i}`}

            height="300px"
            width="100%"

            {...this.props}
        />;
    }
}
