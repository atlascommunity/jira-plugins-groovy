//@flow
import React from 'react';

import Button from '@atlaskit/button';
import {CheckboxStateless} from '@atlaskit/checkbox';

import type {ConsoleResult} from './types';

import {ConsoleMessages} from '../i18n/console.i18n';

import {consoleService} from '../service/services';
import {Bindings} from '../common/bindings';

import {CheckedEditorField, ErrorMessage} from '../common/ak';
import {CommonMessages} from '../i18n/common.i18n';

import './ScriptConsole.less';


const bindings = [Bindings.currentUser];

type Props = {

};

type State = {
    script: ?string,
    isHtml: boolean,
    output: ?ConsoleResult,
    error: ?*,
    waiting: boolean,
};

export class ScriptConsole extends React.Component<Props, State> {
    state = {
        script: '',
        isHtml: false,
        output: null,
        error: null,
        waiting: false
    };

    _scriptChange = (script: ?string) => {
        this.setState({
            script: script
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

    _toggleHtml = () => this.setState(({isHtml}) => ({ isHtml: !isHtml }));

    _submit = () => {
        this.setState({ waiting: true });

        consoleService
            .executeScript(this.state.script || '')
            .then(
                output => this.setState({
                    output,
                    error: null,
                    waiting: false,
                }),
                (err: *) => {
                    const {response} = err;

                    if (response.status === 400 || response.status === 500) {
                        this.setState({
                            error: response.data,
                            output: null,
                            waiting: false,
                        });
                    } else {
                        this.setState({
                            output: null,
                            waiting: false,
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
        const {script, isHtml, output, error, waiting} = this.state;

        let errorMessage: * = null;

        if (error) {
            if (error.field === 'script' && Array.isArray(error.error)) {
                const errors = error.error.filter(e => e);
                errorMessage = errors
                    .map(error => error.message)
                    .map(error => <p key={error}>{error}</p>);
            } else {
                errorMessage = error.message;
            }
        }

        return (
            <div className="ScriptConsole">
                <CheckedEditorField
                    label={CommonMessages.script}
                    scriptType="CONSOLE"

                    resizable={true}
                    bindings={bindings}

                    value={script}
                    onChange={this._scriptChange}
                />
                <CheckboxStateless
                    label={CommonMessages.renderAsHtml}

                    isChecked={isHtml}
                    onChange={this._toggleHtml}
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
                {!waiting && <div className="result" style={{maxWidth: '100%', overflow: 'hidden'}}>
                    {output ?
                        <div>
                            <strong>{ConsoleMessages.executedIn(output.time.toString())}</strong>
                            {isHtml ?
                                <div dangerouslySetInnerHTML={{ __html: output.result }}/>:
                                <pre style={{ overflowX: 'auto' }}>{output.result}</pre>
                            }
                        </div>
                    : null}
                    {error ?
                        <ErrorMessage title={errorMessage || undefined}>
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
