//@flow
import React, {Fragment, type Node} from 'react';

import memoizeOne from 'memoize-one';

import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/diff/diff';
import 'codemirror/mode/velocity/velocity';
import 'codemirror/addon/selection/mark-selection';
import 'codemirror/addon/lint/lint';
import 'codemirror/addon/fold/foldcode';
import 'codemirror/addon/fold/foldgutter';
import 'codemirror/addon/fold/brace-fold';
import 'codemirror/addon/fold/xml-fold';
import 'codemirror/addon/fold/comment-fold';


import Button from '@atlaskit/button';
import Lozenge from '@atlaskit/lozenge';
import Tooltip from '@atlaskit/tooltip';
import InlineMessage from '@atlaskit/inline-message';
import {Label} from '@atlaskit/field-base';
import Spinner from '@atlaskit/spinner';
import {colors} from '@atlaskit/theme';

import QuestionIcon from '@atlaskit/icon/glyph/question';
import QuestionCircleIcon from '@atlaskit/icon/glyph/question-circle';
import CrossCircleIcon from '@atlaskit/icon/glyph/cross-circle';
import CheckCircleIcon from '@atlaskit/icon/glyph/check-circle';
import WarningIcon from '@atlaskit/icon/glyph/jira/failed-build-status';

import {Resizable} from 'react-resizable';

import type {BindingType, ReturnType, MarkerType} from './types';

import {CodeMirror} from './CM';

import {globalBindings} from '../bindings';

import {preferenceService} from '../../service/services';
import {CommonMessages} from '../../i18n/common.i18n';

import './Editor.less';


function isLight(): boolean {
    return !(preferenceService.get('ru.mail.groovy.isLight') === 'false');
}

export type CodeMirrorType = {
    setSize: (width?: number|string, height: number|string) => void,
    performLint: () => void
};

type ResizeCallbackData = {
    node: HTMLElement,
    size: {width: number, height: number}
};

type CodeMirrorOptions = any;

type EditorPosition = {
    line: number,
    ch: number
};

export type AnnotationType = {
    message: string,
    severity: string,
    from: EditorPosition,
    to: EditorPosition
};

export type ValidationState = 'waiting' | 'valid' | 'checkFailed' | 'hasErrors' | 'hasWarnings';

type LinterType = (value: string, callback: ($ReadOnlyArray<AnnotationType>) => void) => void;

type EditorProps = {|
    mode: string,
    value?: string,
    onChange?: (string) => void,
    isDisabled?: boolean,
    markers?: $ReadOnlyArray<MarkerType>,
    bindings?: $ReadOnlyArray<BindingType>,
    returnTypes?: $ReadOnlyArray<ReturnType>,
    readOnly?: boolean,
    decorated?: boolean,
    resizable?: boolean,
    decorator?: (Node) => Node,
    linter?: LinterType,
    validationState?: ValidationState,
    editorDidMount?: (editor: CodeMirrorType) => void
|};

type EditorState = {|
    isLight: boolean,
    height: number
|};

export function transformMarkers(markers?: $ReadOnlyArray<MarkerType>): $ReadOnlyArray<AnnotationType> {
    if (markers) {
        return markers.map(
            (marker: MarkerType): AnnotationType => {
                return {
                    message: marker.message,
                    severity: marker.severity,
                    from: {line: marker.startRow, ch: marker.startCol},
                    to: {line: marker.endRow, ch: marker.endCol}
                };
            }
        );
    }
    return [];
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

    _setEditor = (editor: CodeMirrorType) => {
        this.cm = editor;

        if (this.props.editorDidMount) {
            this.props.editorDidMount(editor);
        }
    };

    _switchTheme = (e: SyntheticEvent<any>) => {
        if (e) {
            e.preventDefault();
        }

        this.setState(
            state => ({
                isLight: !state.isLight
            }),
            () => preferenceService.put('ru.mail.groovy.isLight', this.state.isLight.toString())
        );
    };

    _onChange = (_editor: CodeMirrorType, _data: any, value: string) => {
        if (this.props.onChange) {
            this.props.onChange(value);
        }
    };

    _getAnnotations = (): $ReadOnlyArray<AnnotationType> => {
        return transformMarkers(this.props.markers);
    };

    _resize = (_e: any, {size}: ResizeCallbackData) => {
        this.setState({height: size.height});
    };

    componentDidUpdate(_prevProps: EditorProps, prevState: EditorState) {
        if (prevState.height !== this.state.height) {
            if (this.cm) {
                this.cm.setSize(undefined, this.state.height);
            }
        }
    }

    _getOptions = memoizeOne(
        (readOnly?: boolean, isDisabled?: boolean, mode?: string, isLight: boolean, linter?: LinterType): CodeMirrorOptions => {
            return {
                theme: isLight ? 'eclipse' : 'lesser-dark',
                mode: mode,
                readOnly: readOnly || isDisabled || false,
                lint: {
                    getAnnotations: linter || this._getAnnotations,
                    async: !!linter,
                    tooltips: true
                },
                lineNumbers: true,
                foldGutter: true,
                gutters: ['CodeMirror-lint-markers', 'CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
                //todo: remove for now, too big performance hit
                //viewportMargin: Infinity
            };
        }
    );

    _renderValidationIcon() {
        switch (this.props.validationState) {
            case 'hasErrors':
                return <CrossCircleIcon primaryColor={colors.R400}/>;
            case 'hasWarnings':
                return <WarningIcon primaryColor={colors.Y400}/>;
            case 'checkFailed':
                return <QuestionCircleIcon primaryColor={colors.R400} />;
            case 'valid':
                return <CheckCircleIcon primaryColor={colors.G400}/>;
            case 'waiting':
                return <Spinner/>;
            default:
                return null;
        }
    }

    render() {
        const {onChange, value, bindings, returnTypes, decorated, resizable, decorator, readOnly, isDisabled, mode, validationState, linter} = this.props;
        const {isLight} = this.state;

        const options = this._getOptions(readOnly, isDisabled, mode, isLight, linter);

        let el: Node = <CodeMirror
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
                <div className="EditorInfo">
                    <div style={{color: 'grey'}}>
                        {CommonMessages.editorMode}{' '}
                        <strong>{options.mode}</strong>
                    </div>
                    <div className="flex-grow"/>
                    <div className="flex-vertical-middle">
                        <Button appearance="link" spacing="none" onClick={this._switchTheme}>
                            {CommonMessages.switchTheme}
                        </Button>
                    </div>
                    { bindings &&
                        <div style={{marginLeft: '4px'}}>
                            <InlineMessage type="info" position="top right">
                                <div className="flex-column">
                                    {globalBindings.map(binding => <Binding key={binding.name} binding={binding}/>)}
                                    <hr className="full-width"/>
                                    {bindings.map(binding => <Binding key={binding.name} binding={binding}/>)}
                                    {returnTypes &&
                                        <Fragment>
                                            <hr className="full-width"/>
                                            <Label label={CommonMessages.returnTypes} isFirstChild={true}/>
                                            {returnTypes.map((e, i) =>
                                                <div className="flex-row" key={i}>
                                                    {e.label &&
                                                        <Fragment>
                                                            <div className="flex-none">
                                                                <Lozenge>{e.label}</Lozenge>
                                                            </div>
                                                            <div className="flex-grow"/>
                                                        </Fragment>
                                                    }
                                                    {e.optional &&
                                                        <div className="flex-vertical-middle">
                                                            <Tooltip content="Optional">
                                                                <QuestionIcon size="small" label="optional"/>
                                                            </Tooltip>
                                                        </div>
                                                    }
                                                    <div className="flex-none" style={{marginLeft: '5px'}}>
                                                        {e.javaDoc ?
                                                            <a
                                                                href={e.javaDoc}
                                                                title={e.fullClassName}

                                                                target="_blank"
                                                                rel="noopener noreferrer"
                                                            >
                                                                {e.className}
                                                            </a> :
                                                            <abbr title={e.fullClassName}>{e.className}</abbr>
                                                        }
                                                    </div>
                                                </div>
                                            )}
                                        </Fragment>
                                    }
                                </div>
                            </InlineMessage>
                        </div>
                    }
                    {validationState && <div style={{marginLeft: '4px'}}>{this._renderValidationIcon()}</div>}
                </div>
            </div>
        );
    }
}

type BindingProps = {
    binding: BindingType
};

function Binding({binding}: BindingProps): Node {
    return (
        <div className="flex-row">
            <div className="flex-none">{binding.name}</div>
            <div className="flex-grow"/>
            <div className="flex-none" style={{marginLeft: '5px'}}>
                {binding.javaDoc ?
                    <a
                        href={binding.javaDoc}
                        title={binding.fullClassName}
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        {binding.className}
                    </a> :
                    <abbr title={binding.fullClassName}>{binding.className}</abbr>
                }
            </div>
        </div>
    );
}
