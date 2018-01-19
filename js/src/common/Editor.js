import React from 'react';
import PropTypes from 'prop-types';

import 'brace';

import 'brace/mode/groovy';
import 'brace/mode/diff';
import 'brace/theme/xcode';
import 'brace/theme/monokai';

import Ace from 'react-ace';

import {preferenceService} from '../service/services';

import './Editor.less';
import {CommonMessages} from '../i18n/common.i18n';


let count = 0;

function isLight() {
    return !(preferenceService.get('ru.mail.groovy.isLight') === 'false');
}

export class Editor extends React.Component {
    static propTypes = {
        mode: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired
    };

    state = {
        isLight: isLight()
    };

    _switchTheme = (e) => {
        if (e) {
            e.preventDefault();
        }

        const value = !this.state.isLight;
        preferenceService.put('ru.mail.groovy.isLight', value);
        this.setState({
            isLight: value
        });
    };

    componentDidMount() {
        this.i = count++;
    }

    render() {
        return (
            <div className="flex-column">
                <Ace
                    theme={this.state.isLight ? 'xcode' : 'monokai'}
                    name={`ace-editor-${this.i}`}

                    height="300px"
                    width="100%"

                    {...this.props}
                />
                <div className="flex-row" style={{justifyContent: 'flex-end', marginRight: '3px'}}>
                    <a href="" onClick={this._switchTheme}>
                        {CommonMessages.switchTheme}
                    </a>
                </div>
            </div>
        );
    }
}
