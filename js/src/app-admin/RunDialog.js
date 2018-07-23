//@flow
import React, {type Node} from 'react';

import memoizeOne from 'memoize-one';

import {Map} from 'immutable';
import type {Map as MapType} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';

import type {FunctionType} from '@atlaskit/modal-dialog/dist/cjs/types';

import {PropField} from './PropField';

import type {AdminScriptOutcomeType, AdminScriptType} from './types';

import {CommonMessages} from '../i18n/common.i18n';
import {AdminScriptMessages} from '../i18n/admin.i18n';

import type {VoidCallback} from '../common/types';
import type {ParamType} from '../app-workflow/types';
import {adminScriptService} from '../service/services';
import {LoadingSpinner} from '../common/ak/LoadingSpinner';
import type {SingleValueType} from '../common/ak/types';
import {ErrorMessage, InfoMessage, SuccessMessage} from '../common/ak/messages';


type Props = {
    script: AdminScriptType,
    onClose: VoidCallback
};

type State = {
    stage: 'params' | 'running' | 'done',
    values: MapType<string, any>,
    outcome: ?AdminScriptOutcomeType,
};

const selectTypes = ['CUSTOM_FIELD', 'USER', 'GROUP', 'RESOLUTION'];


export class RunDialog extends React.PureComponent<Props, State> {
    state = {
        stage: 'params',
        values: Map(),
        outcome: null
    };

    _run = () => {
        const {script} = this.props;
        const {values} = this.state;

        this.setState({
            stage: 'running',
            outcome: null
        });

        const params = {};

        if (script.params) {
            for (const param of script.params) {
                let value: ?string|?SingleValueType|boolean = values.get(param.name);

                if (selectTypes.includes(param.paramType) && typeof value !== 'string' && typeof value !== 'boolean') {
                    value = value ? value.value : null;
                } else if (param.paramType === 'BOOLEAN') {
                    value = value || false;
                }

                params[param.name] = value;
            }
        }

        const promise = script.builtIn && script.builtInKey ?
            adminScriptService.runBuiltInScript(script.builtInKey, params) :
            adminScriptService.runUserScript(script.id, params);

        promise
            .then(outcome => this.setState({
                outcome,
                stage: 'done'
            }));
    };

    _updateValue = memoizeOne((field: string) => (value: any) => this.setState((state: State): * => {
        return {
            values: state.values.set(field, value)
        };
    }));

    _renderFields = (): Node => {
        const {params} = this.props.script;

        if (!params || !params.length) {
            return <InfoMessage title={AdminScriptMessages.noParams}/>;
        }

        return (
            <div className="flex-column">
                {params.map((param: ParamType): Node =>
                    <PropField
                        key={param.name}
                        label={param.displayName}
                        //$FlowFixMe
                        type={param.paramType}
                        onChange={this._updateValue(param.name)}
                        value={(this.state.values.get(param.name): any)}
                    />
                )}
            </div>
        );
    };

    _getActions = (): Array<{onClick: FunctionType, text: string}> => {
        const {onClose} = this.props;
        const {stage, outcome} = this.state;

        switch (stage) {
            case 'params':
                return [
                    {
                        text: CommonMessages.run,
                        onClick: this._run,
                    },
                    {
                        text: CommonMessages.cancel,
                        onClick: onClose,
                    }
                ];
            case 'done':
                if (outcome && outcome.success) {
                    return [
                        {
                            text: CommonMessages.close,
                            onClick: onClose,
                        },
                        {
                            text: AdminScriptMessages.runAgain,
                            onClick: this._run
                        }
                    ];
                } else {
                    return [
                        {
                            text: CommonMessages.back,
                            onClick: () => this.setState({ stage: 'params' })
                        },
                        {
                            text: CommonMessages.close,
                            onClick: onClose,
                        }
                    ];
                }
            default:
                return [];
        }
    };

    render() {
        const {script, onClose} = this.props;
        const {stage, outcome} = this.state;

        let content: ?Node = null;

        if (stage === 'params') {
            content = this._renderFields();
        } else if (stage === 'running') {
            content = <LoadingSpinner/>;
        } else if (stage === 'done' && outcome) {
            if (outcome.success) {
                content = <SuccessMessage title="Done">{outcome.message}</SuccessMessage>;
            } else {
                content = <ErrorMessage title="Error occurred">{outcome.message}</ErrorMessage>;
            }
        }

        return (
            <ModalDialog
                width="small"
                scrollBehavior="outside"

                isHeadingMultiline={false}
                heading={script.name}

                onClose={stage !== 'running' && onClose}
                actions={this._getActions()}
            >
                {content}
            </ModalDialog>
        );
    }
}
