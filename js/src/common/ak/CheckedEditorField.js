//@flow
import React, {type ElementConfig} from 'react';

import {EditorField} from './EditorField';

import type {EntityType} from '../types';
import {extrasService} from '../../service/services';
import {getMarkers} from '../error';
import {transformMarkers} from '../editor/Editor';
import type {AnnotationType} from '../editor/Editor';


type State = {|
    isLoading: boolean,
    hasErrors: boolean
|};

type Props = ElementConfig<typeof EditorField> & {
    scriptType: EntityType | 'CONSOLE',
    typeParams?: {[string]: string}
};

//todo: indicate loading
//todo: почему-то редактор тупит
export class CheckedEditorField extends React.Component<Props, State> {
    state = {
        isLoading: false,
        hasErrors: false
    };

    reqId = 0;

    _checkScript = (value: string, callback: ($ReadOnlyArray<AnnotationType>) => void) => {
        const {scriptType} = this.props;

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
                }
            })
            .catch((e: *) => {
                const {response} = e;

                if (response.status === 400) {
                    this.setState({ isLoading: false, hasErrors: true });
                    callback(transformMarkers(getMarkers(response.data.error)));
                } else {
                    this.setState({ isLoading: false, hasErrors: false });
                    callback([]);
                    throw e;
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
