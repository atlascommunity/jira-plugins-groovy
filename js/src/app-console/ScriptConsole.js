import React from 'react';

import Message from 'aui-react/lib/AUIMessage';

import {ConsoleMessages} from '../i18n/console.i18n';

import {consoleService} from '../service/services';
import {Editor} from '../common/Editor';

//todo: add spinner
export class ScriptConsole extends React.Component {
    state = {
        script: '',
        output: null,
        error: null
    };

    _scriptChange = (script) => this.setState({script: script});

    _submit = (e) => {
        if (e) {
            e.preventDefault();
        }

        consoleService
            .executeScript(this.state.script)
            .then(
                output => this.setState({output, error: null}),
                ({response}) => {
                    if (response.status === 500) {
                        this.setState({
                            error: response.data,
                            output: null
                        });
                    }
                }
            );
    };

    render() {
        const state = this.state;

        return (
            <form className="aui flex-column" onSubmit={this._submit}>
                <Editor
                    mode="groovy"

                    onChange={this._scriptChange}
                    value={state.script}
                />
                <br/>
                <div>
                    <button className="button">{ConsoleMessages.execute}</button>
                </div>
                <br/>
                <div id="console-result">
                    {state.output ?
                        <div>
                            <strong>{ConsoleMessages.executedIn(state.output.time)}</strong>
                            <pre>{state.output.result}</pre>
                        </div>
                    : null}
                    {state.error ?
                        <Message type="error" title={state.error.message}>
                            <pre style={{overflowX: 'auto'}}>
                                {state.error['stack-trace']}
                            </pre>
                        </Message>
                    : null}
                </div>
            </form>
        );
    }
}
