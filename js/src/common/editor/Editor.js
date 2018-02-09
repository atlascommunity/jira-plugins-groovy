import React from 'react';
import PropTypes from 'prop-types';

import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/diff/diff';
import 'codemirror/mode/velocity/velocity';
import 'codemirror/addon/selection/mark-selection';
import 'codemirror/addon/lint/lint';

import InlineMessage from '@atlaskit/inline-message';

import {Controlled as CodeMirror} from 'react-codemirror2';
import {Resizable} from 'react-resizable';

import {globalBindings} from '../bindings';

import {preferenceService} from '../../service/services';
import {CommonMessages} from '../../i18n/common.i18n';

import './Editor.less';


function isLight() {
    return !(preferenceService.get('ru.mail.groovy.isLight') === 'false');
}

export const BindingShape = PropTypes.shape({
    name: PropTypes.string.isRequired,
    className: PropTypes.string.isRequired,
    fullClassName: PropTypes.string.isRequired
});

export const MarkerShape = PropTypes.shape({
    startRow: PropTypes.number,
    endRow: PropTypes.number,
    startCol: PropTypes.number,
    endCol: PropTypes.number,
    className: PropTypes.string
});

//todo: remember height for console
//todo: change to PureComponent
export class Editor extends React.Component {
    static propTypes = {
        mode: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired,
        onChange: PropTypes.func,
        isDisabled: PropTypes.bool,
        markers: PropTypes.arrayOf(MarkerShape.isRequired),
        bindings: PropTypes.arrayOf(BindingShape.isRequired),
        readOnly: PropTypes.bool,
        decorated: PropTypes.bool,
        resizable: PropTypes.bool,
        decorator: PropTypes.func
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

    componentWillUpdate(_props, state) {
        if (this.state.isLight !== state.isLight) {
            this.options = null;
        }
    }

    componentDidUpdate(_prevProps, prevState) {
        if (prevState.height !== this.state.height) {
            this.cm.setSize(null, this.state.height);
        }
    }

    componentWillReceiveProps(props) {
        const {readOnly, isDisabled, mode} = this.props;

        const invalidateOptions = props.readOnly !== readOnly || props.isDisabled !== isDisabled || props.mode !== mode;

        if (invalidateOptions) {
            this.options = null;
            //todo: consider setting options through cm instance
        }
    }

    options = null;

    _getOptions = () => {
        if (!this.options) {
            const {readOnly, isDisabled, mode} = this.props;
            const {isLight} = this.state;

            this.options = {
                theme: isLight ? 'eclipse' : 'lesser-dark',
                mode: mode,
                lineNumbers: true,
                readOnly: readOnly || isDisabled || false,
                gutters: ['CodeMirror-lint-markers'],
                lint: {
                    getAnnotations: this._getAnnotations,
                    tooltips: true
                },
                //todo: remove for now, too big performance hit viewportMargin: Infinity
            };
        }
        return this.options;
    };

    render() {
        const {onChange, value, bindings, decorated, resizable, decorator} = this.props;

        const options = this._getOptions();
        let el = <CodeMirror
            options={options}

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

        if (decorator) {
            el = decorator(el);
        }

        return (
            <div className="Editor">
                <div className={`CodeEditor ${decorated ? 'DecoratedEditor' : ''}`}>
                    {el}
                </div>
                <div className="flex-row">
                    <div style={{color: 'grey'}}>
                        {CommonMessages.editorMode}{' '}
                        <strong>{options.mode}</strong>
                    </div>
                    <div className="flex-grow"/>
                    <div className="flex-vertical-middle">
                        <a href="" onClick={this._switchTheme}>
                            {CommonMessages.switchTheme}
                        </a>
                    </div>
                    { bindings &&
                        <div style={{marginLeft: '5px'}}>
                            <InlineMessage type="info" position="top right">
                                <div className="flex-column">
                                    {globalBindings.map(binding => <Binding key={binding.name} binding={binding}/>)}
                                    <hr className="full-width"/>
                                    {bindings.map(binding => <Binding key={binding.name} binding={binding}/>)}
                                </div>
                            </InlineMessage>
                        </div>
                    }
                </div>
            </div>
        );
    }
}

function Binding({binding}) {
    return (
        <div className="flex-row">
            <div className="flex-none">{binding.name}</div>
            <div className="flex-grow"/>
            <div className="flex-none" style={{marginLeft: '5px'}}>
                <abbr title={binding.fullClassName}>{binding.className}</abbr>
            </div>
        </div>
    );
}

Binding.propTypes = {
    binding: BindingShape.isRequired
};
