//@flow
import React from 'react';

import Button from '@atlaskit/button';

import type {ConsoleResult} from './types';

import {ConsoleMessages} from '../i18n/console.i18n';

import {consoleService} from '../service/services';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';

import {EditorField, ErrorMessage} from '../common/ak';
import {CommonMessages} from '../i18n/common.i18n';

import './ScriptConsole.less';


const bindings = [Bindings.currentUser];

type Props = {

};

type State = {
    script: ?string,
    output: ?ConsoleResult,
    error: ?*,
    waiting: boolean,
    modified: boolean
};

export class ScriptConsole extends React.Component<Props, State> {
    state = {
        script: '',
        output: null,
        error: null,
        waiting: false,
        modified: false
    };

    _scriptChange = (script: ?string) => {
        this.setState({
            script: script,
            modified: true
        });

        try {
            if (sessionStorage) {
                if (script) {
                    sessionStorage.setItem('my-groovy-console-script', script);
                } else {
                    sessionStorage.removeItem('my-groovy-console-script');
                }
            }
        } catch (e) {
            console.error('unable to save script', e);
        }
    };

    _submit = () => {
        this.setState({ waiting: true });

        consoleService
            .executeScript(this.state.script || '')
            .then(
                output => this.setState({
                    output,
                    error: null,
                    waiting: false,
                    modified: false
                }),
                (err: *) => {
                    const {response} = err;

                    if (response.status === 400 || response.status === 500) {
                        this.setState({
                            error: response.data,
                            output: null,
                            waiting: false,
                            modified: false
                        });
                    } else {
                        this.setState({
                            output: null,
                            waiting: false,
                            modified: false
                        });

                        throw err;
                    }
                }
            );
    };

    componentDidMount() {
        if (sessionStorage) {
            const script = sessionStorage.getItem('my-groovy-console-script');

            if (script) {
                this.setState({ script });
            }
        }
    }

    render() {
        const {script, output, error, waiting, modified} = this.state;

        let errorMessage: * = null;
        let markers: * = null;

        if (error) {
            if (error.field === 'script' && Array.isArray(error.error)) {
                const errors = error.error.filter(e => e);
                if (!modified) {
                    markers = getMarkers(errors);
                }
                errorMessage = errors
                    .map(error => error.message)
                    .map(error => <p key={error}>{error}</p>);
            } else {
                errorMessage = error.message;
            }
        }

        return (
            <div className="ScriptConsole">
                <EditorField
                    label={CommonMessages.script}

                    resizable={true}
                    markers={markers}
                    bindings={bindings}

                    value={script}
                    onChange={this._scriptChange}
                />
                <br/>
                <div>
                    <Button
                        appearance="primary"
                        isDisabled={waiting}
                        isLoading={waiting}
                        onClick={this._submit}
                    >
                        {ConsoleMessages.execute}
                        </Button>
                </div>
                <br/>
                {!waiting && <div className="result">
                    {output ?
                        <div>
                            <strong>{ConsoleMessages.executedIn(output.time.toString())}</strong>
                            <pre>{output.result}</pre>
                        </div>
                    : null}
                    {error ?
                        <ErrorMessage title={errorMessage}>
                            <pre style={{overflowX: 'auto'}}>
                                {error['stack-trace']}
                            </pre>
                        </ErrorMessage>
                    : null}
                </div>}
            </div>
        );
    }
}
