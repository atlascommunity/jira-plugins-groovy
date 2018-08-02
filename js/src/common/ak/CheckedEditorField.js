//@flow
import React, {type ElementConfig} from 'react';

import {EditorField} from './EditorField';

import {extrasService} from '../../service/services';
import {getMarkers} from '../error';
import {transformMarkers} from '../editor/Editor';
import type {AnnotationType} from '../editor/Editor';


type State = {|
    isLoading: boolean,
    hasErrors: boolean
|};

type Props = ElementConfig<typeof EditorField> & {
    scriptType: 'CONSOLE' | 'WORKFLOW_GENERIC',
    typeParams?: {[string]: string}
};

type AnnotationsType = $ReadOnlyArray<AnnotationType>;

export class CheckedEditorField extends React.Component<Props, State> {
    state = {
        isLoading: false,
        hasErrors: false
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
            this.setState({ isLoading: false, hasErrors: false });
            callback([]);
            return;
        }

        let promise: ?Promise<AnnotationsType> = this.cachedPromise;

        if (!promise) {
            this.setState({ isLoading: true });

            promise = this.cachedPromise = extrasService
                .checkScript(value, scriptType)
                .then((): AnnotationsType => {
                    if (value === this.lastRequestedValue) {
                        this.setState({isLoading: false, hasErrors: false});
                        return [];
                    }
                    return [];
                })
                .catch((e: *): AnnotationsType => {
                    if (value === this.lastRequestedValue) {
                        const {response} = e;

                        if (response.status === 400) {
                            this.setState({isLoading: false, hasErrors: true});
                            return transformMarkers(getMarkers(response.data.error));
                        } else {
                            this.setState({isLoading: false, hasErrors: false});
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
        const {isLoading, hasErrors} = this.state;

        return (<EditorField
            {...props}
            linter={this._checkScript}
            validationState={isLoading ? 'loading' : hasErrors ? 'invalid' : 'valid'}
        />);
    }
}
