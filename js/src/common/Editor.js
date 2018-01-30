import React from 'react';
import PropTypes from 'prop-types';

import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/diff/diff';
import 'codemirror/addon/selection/mark-selection';
import 'codemirror/addon/lint/lint';

import {Controlled as CodeMirror} from 'react-codemirror2';
import {Resizable} from 'react-resizable';

import {preferenceService} from '../service/services';

import './Editor.less';

import {CommonMessages} from '../i18n/common.i18n';


function isLight() {
    return !(preferenceService.get('ru.mail.groovy.isLight') === 'false');
}

//todo: remember height for console
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
        resizable: PropTypes.bool
    };

    cm = null;
    state = {
        isLight: isLight(),
        height: 300
    };

    _setEditor = (editor) => this.cm = editor;

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

    _resize = (_e, {size}) => {
        console.log(size);
        this.setState({height: size.height});
    };

    componentDidUpdate(_prevProps, prevState) {
        if (prevState.height !== this.state.height) {
            this.cm.setSize(null, this.state.height);
        }
    }

    render() {
        const {onChange, value, readyOnly, mode, decorated, resizable} = this.props;

        let el = <CodeMirror
            options={{
                theme: this.state.isLight ? 'eclipse' : 'lesser-dark',
                mode: mode,
                lineNumbers: true,
                readOnly: readyOnly || false,
                gutters: ['CodeMirror-lint-markers'],
                lint: {
                    getAnnotations: this._getAnnotations,
                    tooltips: true
                },
                viewportMargin: Infinity
            }}

            onBeforeChange={onChange && this._onChange}
            value={value}
            editorDidMount={this._setEditor}
        />;

        if (resizable) {
            el =
                <Resizable height={this.state.height} width={100} axis="y" onResize={this._resize}>
                    <div style={{width: '100%', height: `${this.state.height}px`, overflow: 'hidden'}}>
                        {el}
                    </div>
                </Resizable>;
        }

        return (
            <div className="Editor">
                <div className={`CodeEditor ${decorated ? 'DecoratedEditor' : ''}`}>
                    {el}
                </div>
                <div className="flex-row" style={{margin: '0 3px'}}>
                    <div style={{color: 'grey'}}>
                        <strong>{CommonMessages.editorMode}{':'}</strong> {mode}
                    </div>
                    <div className="flex-grow"/>
                    <div>
                        <a href="" onClick={this._switchTheme}>
                            {CommonMessages.switchTheme}
                        </a>
                    </div>
                </div>
            </div>
        );
    }
}
