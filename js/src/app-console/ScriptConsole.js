import React from 'react';

import Message from 'aui-react/lib/AUIMessage';
import Button from '@atlaskit/button';

import {ConsoleMessages} from '../i18n/console.i18n';

import {consoleService} from '../service/services';
import {Editor} from '../common/Editor';
import {getMarkers} from '../common/error';
import {Bindings} from '../common/bindings';

import './ScriptConsole.less';


export class ScriptConsole extends React.Component {
    state = {
        script: '',
        output: null,
        error: null,
        waiting: false,
        modified: false
    };

    _scriptChange = (script) => this.setState({
        script: script,
        modified: true
    });

    _submit = (e) => {
        if (e) {
            e.preventDefault();
        }

        this.setState({ waiting: true });

        consoleService
            .executeScript(this.state.script)
            .then(
                output => this.setState({
                    output,
                    error: null,
                    waiting: false,
                    modified: false
                }),
                (err) => {
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

    render() {
        const {script, output, error, waiting, modified} = this.state;

        let errorMessage = null;
        let markers = null;

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
                <Editor
                    mode="groovy"
                    resizable={true}
                    bindings={[
                        Bindings.currentUser
                    ]}

                    onChange={this._scriptChange}
                    value={script}

                    markers={markers}
                />
                <br/>
                <div>
                    <Button appearance="primary" isDisabled={waiting} onClick={this._submit}>{ConsoleMessages.execute}</Button>
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
                        <Message type="error" title={errorMessage}>
                            <pre style={{overflowX: 'auto'}}>
                                {error['stack-trace']}
                            </pre>
                        </Message>
                    : null}
                </div>}
                {waiting && <div className="aui-icon aui-icon-wait"/>}
            </div>
        );
    }
}
