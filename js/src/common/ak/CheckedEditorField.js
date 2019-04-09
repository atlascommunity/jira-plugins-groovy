//@flow
import React, {type ElementConfig} from 'react';

import isEqual from 'lodash/isEqual';

import {EditorField} from './EditorField';

import {extrasService} from '../../service';
import {getMarkers} from '../error';
import {transformMarkers} from '../editor';

import type {AnnotationType, CodeMirrorType, ValidationState} from '../editor';
import type {SyntaxError} from '../types';


type State = {|
    validationState: ValidationState
|};

export type StaticCheckScriptType = 'CONSOLE' | 'WORKFLOW_GENERIC' | 'ADMIN_SCRIPT' | 'REST' | 'CUSTOM_FIELD' | 'SCHEDULED_TASK' | 'LISTENER' | 'JQL' | 'GLOBAL_OBJECT';

type Props = {|
    ...ElementConfig<typeof EditorField>,
    scriptType: StaticCheckScriptType,
    typeParams?: {[string]: string}
|};

type AnnotationsType = $ReadOnlyArray<AnnotationType>;

export class CheckedEditorField extends React.Component<Props, State> {
    state = {
        validationState: 'valid'
    };

    cm = null;
    lastRequestedValue = null;
    cachedPromise = null;

    _setEditor = (cm: CodeMirrorType) => {
        this.cm = cm;
    };

    _checkScript = (value: string, callback: ($ReadOnlyArray<AnnotationType>) => void) => {
        const {scriptType, typeParams} = this.props;

        if (this.lastRequestedValue !== value) {
            this.cachedPromise = null;
        }

        this.lastRequestedValue = value;

        if (!value) {
            this.setState({ validationState: 'valid' });
            callback([]);
            return;
        }

        let promise: ?Promise<AnnotationsType> = this.cachedPromise;

        if (!promise) {
            this.setState({ validationState: 'waiting' });

            promise = this.cachedPromise = extrasService
                .checkScript(value, scriptType, typeParams)
                .then((result: $ReadOnlyArray<SyntaxError>): AnnotationsType => {
                    if (value === this.lastRequestedValue) {
                        const hasErrors = result.some(it => it.type === 'error');

                        this.setState({ validationState: result.length ? (hasErrors ? 'hasErrors' : 'hasWarnings') : 'valid' });
                        return transformMarkers(getMarkers(result));
                    }
                    return [];
                })
                .catch((e: *): AnnotationsType => {
                    if (value === this.lastRequestedValue) {
                        const {response} = e;

                        if (response.status === 400) {
                            this.setState({ validationState: 'hasErrors' });
                            return transformMarkers(getMarkers(response.data.error));
                        } else {
                            this.setState({ validationState: 'checkFailed' });
                            throw e;
                        }
                    }
                    return [];
                });
        }

        if (promise) {
            promise.then(annotations => callback(annotations));
        } else {
            console.error('no promise');
        }
    };

    componentDidUpdate(prevProps: Props) {
        if (!isEqual(prevProps.typeParams, this.props.typeParams)) {
            if (this.cm) {
                this.cachedPromise = null;
                this.lastRequestedValue = null;
                this.cm.performLint();
            }
        }
    }

    render() {
        const {scriptType, typeParams, ...props} = this.props;
        const {validationState} = this.state;

        return (<EditorField
            {...props}
            editorDidMount={this._setEditor}
            linter={this._checkScript}
            validationState={validationState}
        />);
    }
}
