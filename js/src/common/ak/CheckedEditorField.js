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

//todo: разобраться с одновременным запросом двух проверок
export class CheckedEditorField extends React.Component<Props, State> {
    state = {
        isLoading: false,
        hasErrors: false
    };

    reqId = 0;
    lastRequestedValue = null;
    lastResult = [];

    _checkScript = (value: string, callback: ($ReadOnlyArray<AnnotationType>) => void) => {
        const {scriptType} = this.props;

        if (this.lastRequestedValue === value) {
            callback(this.lastResult);
            return;
        }

        if (!value) {
            this.setState({ isLoading: false, hasErrors: false });
            return;
        }

        const currentRequest = ++this.reqId;

        this.setState({ isLoading: true });

        extrasService
            .checkScript(value, scriptType)
            .then(() => {
                if (currentRequest === this.reqId) {
                    this.setState({ isLoading: false, hasErrors: false });
                    callback([]);
                    this.lastRequestedValue = value;
                    this.lastResult = [];
                }
            })
            .catch((e: *) => {
                if (currentRequest === this.reqId) {
                    const {response} = e;

                    if (response.status === 400) {
                        this.setState({ isLoading: false, hasErrors: true });
                        const annotations = transformMarkers(getMarkers(response.data.error));
                        callback(annotations);
                        this.lastRequestedValue = value;
                        this.lastResult = annotations;
                    } else {
                        this.setState({ isLoading: false, hasErrors: false });
                        callback([]);
                        this.lastRequestedValue = value;
                        this.lastResult = [];
                        throw e;
                    }
                }
            });
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
