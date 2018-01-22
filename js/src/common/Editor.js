import React from 'react';
import PropTypes from 'prop-types';

import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/diff/diff';
import 'codemirror/addon/selection/mark-selection';
import 'codemirror/addon/lint/lint';

import {Controlled as CodeMirror} from 'react-codemirror2';

import {preferenceService} from '../service/services';

import './Editor.less';

import {CommonMessages} from '../i18n/common.i18n';


function isLight() {
    return !(preferenceService.get('ru.mail.groovy.isLight') === 'false');
}

export class Editor extends React.Component {
    static propTypes = {
        mode: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired,
        onChange: PropTypes.func,
        readyOnly: PropTypes.bool,
        markers: PropTypes.arrayOf(
            PropTypes.shape({
                startRow: PropTypes.number,
                endRow: PropTypes.number,
                startCol: PropTypes.number,
                endCol: PropTypes.number,
                className: PropTypes.string
            })
        ),
        decorated: PropTypes.bool,
        className: PropTypes.string
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

    _onChange = (_editor, _data, value) => this.props.onChange(value);

    _getAnnotations = () => {
        const markers = this.props.markers;
        if (markers) {
            return markers.map(marker => {
                return {
                    message: marker.message,
                    severity: 'error',
                    from: {line: marker.startRow, ch: marker.startCol},
                    to: {line: marker.endRow, ch: marker.endCol}
                };
            });
        }
        return [];
    };

    render() {
        //todo: add eslint rule to ignore unused __vars
       const {onChange, value, readyOnly, mode, decorated, className} = this.props;

        return (
            <div className="flex-column">
                <div className={`${decorated ? 'DecoratedEditor' : ''}`}>
                    <CodeMirror
                        options={{
                            theme: this.state.isLight ? 'eclipse' : 'lesser-dark',
                            mode: mode,
                            lineNumbers: true,
                            readOnly: readyOnly || false,
                            gutters: ['CodeMirror-lint-markers'],
                            lint: {
                                getAnnotations: this._getAnnotations,
                                tooltips: true
                            }
                        }}

                        height="300px"
                        width="100%"

                        onBeforeChange={onChange && this._onChange}
                        value={value}
                    />
                </div>
                <div className="flex-row" style={{justifyContent: 'flex-end', marginRight: '3px'}}>
                    <a href="" onClick={this._switchTheme}>
                        {CommonMessages.switchTheme}
                    </a>
                </div>
            </div>
        );
    }
}
