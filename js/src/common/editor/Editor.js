//@flow
import * as React from 'react';

import memoizeOne from 'memoize-one';

import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/diff/diff';
import 'codemirror/mode/velocity/velocity';
import 'codemirror/addon/selection/mark-selection';
import 'codemirror/addon/lint/lint';

import InlineMessage from '@atlaskit/inline-message';

import {Resizable} from 'react-resizable';

import type {BindingType, MarkerType} from './types';

import {CodeMirror} from './CM';

import {globalBindings} from '../bindings';

import {preferenceService} from '../../service/services';
import {CommonMessages} from '../../i18n/common.i18n';

import './Editor.less';


function isLight() : boolean {
    return !(preferenceService.get('ru.mail.groovy.isLight') === 'false');
}

type CodeMirrorType = {
    setSize: (width?: number|string, height: number|string) => void
};

type ResizeCallbackData = {
    node: HTMLElement,
    size: {width: number, height: number}
};

type EditorProps = {
    mode: string,
    value?: string,
    onChange?: (string) => void,
    isDisabled?: boolean,
    markers?: Array<MarkerType>,
    bindings?: Array<BindingType>,
    readOnly?: boolean,
    decorated?: boolean,
    resizable?: boolean,
    decorator?: (React.Node) => React.Node
}

type EditorState = {
    isLight: boolean,
    height: number
}

//todo: remember height for console
//todo: change to PureComponent
//todo: move theme state to redux
export class Editor extends React.Component<EditorProps, EditorState> {
    cm: ?CodeMirrorType = null;

    state = {
        isLight: isLight(),
        height: 300
    };

    _setEditor = (editor : CodeMirrorType) => this.cm = editor;

    _switchTheme = (e : SyntheticEvent<any>) => {
        if (e) {
            e.preventDefault();
        }

        const value = !this.state.isLight;
        preferenceService.put('ru.mail.groovy.isLight', value);
        this.setState({
            isLight: value
        });
    };

    _onChange = (_editor:any, _data:any, value:string) => {
        if (this.props.onChange) {
            this.props.onChange(value);
        }
    };

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

    _resize = (_e: any, {size}: ResizeCallbackData) => {
        console.log(size);
        this.setState({height: size.height});
    };

    componentDidUpdate(_prevProps : EditorProps, prevState : EditorState) {
        if (prevState.height !== this.state.height) {
            if (this.cm) {
                this.cm.setSize(undefined, this.state.height);
            }
        }
    }

    _getOptions = memoizeOne(
        (readOnly?: boolean, isDisabled?: boolean, mode?: string, isLight: boolean) => {
            return {
                theme: isLight ? 'eclipse' : 'lesser-dark',
                mode: mode,
                lineNumbers: true,
                readOnly: readOnly || isDisabled || false,
                gutters: ['CodeMirror-lint-markers'],
                lint: {
                    getAnnotations: this._getAnnotations,
                    tooltips: true
                },
                //todo: remove for now, too big performance hit
                //viewportMargin: Infinity
            };
        }
    );

    render() {
        const {onChange, value, bindings, decorated, resizable, decorator, readOnly, isDisabled, mode} = this.props;
        const {isLight} = this.state;

        const options = this._getOptions(readOnly, isDisabled, mode, isLight);

        let el = <CodeMirror
            options={options}

            onBeforeChange={onChange && this._onChange}
            value={value || ''}
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

type BindingProps = {
    binding: BindingType
}

function Binding({binding} : BindingProps) : React.Node {
    return (
        <div className="flex-row">
            <div className="flex-none">{binding.name}</div>
            <div className="flex-grow"/>
            <div className="flex-none" style={{marginLeft: '5px'}}>
                {binding.javaDoc ?
                    <a href={binding.javaDoc} title={binding.fullClassName} target="_blank">{binding.className}</a> :
                    <abbr title={binding.fullClassName}>{binding.className}</abbr>
                }
            </div>
        </div>
    );
}
