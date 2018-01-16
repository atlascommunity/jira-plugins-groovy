import React from 'react';

import Message from 'aui-react/lib/AUIMessage';

import {ConsoleMessages} from '../i18n/console.i18n';

import {consoleService} from '../service/services';
import {Editor} from '../common/Editor';


export class ScriptConsole extends React.Component {
    state = {
        script: '',
        output: null,
        error: null,
        waiting: false
    };

    _scriptChange = (script) => this.setState({script: script});

    _submit = (e) => {
        if (e) {
            e.preventDefault();
        }

        this.setState({ waiting: true });

        consoleService
            .executeScript(this.state.script)
            .then(
                output => this.setState({ output, error: null, waiting: false }),
                ({response}) => {
                    if (response.status === 500) {
                        this.setState({
                            error: response.data,
                            output: null,
                            waiting: false
                        });
                    } else {
                        this.setState({ waiting: false });
                    }
                }
            );
    };

    render() {
        const {script, output, error, waiting} = this.state;

        return (
            <form className="aui flex-column" onSubmit={this._submit}>
                <Editor
                    mode="groovy"

                    onChange={this._scriptChange}
                    value={script}
                />
                <br/>
                <div>
                    <button className="button" disabled={waiting}>{ConsoleMessages.execute}</button>
                </div>
                <br/>
                {!waiting && <div id="console-result">
                    {output ?
                        <div>
                            <strong>{ConsoleMessages.executedIn(output.time)}</strong>
                            <pre>{output.result}</pre>
                        </div>
                    : null}
                    {error ?
                        <Message type="error" title={error.message}>
                            <pre style={{overflowX: 'auto'}}>
                                {error['stack-trace']}
                            </pre>
                        </Message>
                    : null}
                </div>}
                {waiting && <div className="aui-icon aui-icon-wait"/>}
            </form>
        );
    }
}
