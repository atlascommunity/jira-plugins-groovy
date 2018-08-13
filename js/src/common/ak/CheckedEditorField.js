//@flow
import React, {type ElementConfig} from 'react';

import {EditorField} from './EditorField';

import {extrasService} from '../../service/services';
import {getMarkers} from '../error';
import {transformMarkers} from '../editor/Editor';

import type {AnnotationType, ValidationState} from '../editor/Editor';
import type {SyntaxError} from '../types';


type State = {|
    validationState: ValidationState
|};

export type StaticCheckScriptType = 'CONSOLE' | 'WORKFLOW_GENERIC' | 'ADMIN_SCRIPT';

type Props = ElementConfig<typeof EditorField> & {
    scriptType: StaticCheckScriptType,
    typeParams?: {[string]: string}
};

type AnnotationsType = $ReadOnlyArray<AnnotationType>;

export class CheckedEditorField extends React.Component<Props, State> {
    state = {
        validationState: 'valid'
    };

    lastRequestedValue = null;
    cachedPromise = null;

    _checkScript = (value: string, callback: ($ReadOnlyArray<AnnotationType>) => void) => {
        const {scriptType} = this.props;


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
                .checkScript(value, scriptType)
                .then((result: $ReadOnlyArray<SyntaxError>): AnnotationsType => {
                    if (value === this.lastRequestedValue) {
                        this.setState({ validationState: result.length ? 'hasWarnings' : 'valid' });
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

    render() {
        const {scriptType, typeParams, ...props} = this.props;
        const {validationState} = this.state;

        return (<EditorField
            {...props}
            linter={this._checkScript}
            validationState={validationState}
        />);
    }
}
