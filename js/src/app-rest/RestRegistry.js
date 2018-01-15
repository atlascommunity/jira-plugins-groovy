import React from 'react';
import PropTypes from 'prop-types';

import {connect} from 'react-redux';

import Button from 'aui-react/lib/AUIButton';

import {ScriptActionCreators} from './rest.reducer';

import {restService} from '../service/services';
import {RestMessages} from '../i18n/rest.i18n';
import {Script} from '../common/Script';


export class RestRegistry extends React.Component {
    static propTypes = {
        scripts: PropTypes.arrayOf(PropTypes.object).isRequired, //todo: model
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew, id) => () => this.props.triggerDialog(isNew, id);

    render() {
        return (
            <div className="flex-column">
                <div>
                    <Button icon="add" type="primary" onClick={this._triggerDialog(true)}>
                        {RestMessages.addScript}
                    </Button>
                </div>
                <div className="flex-column">
                    {this.props.listeners.map(listener =>
                        <RestScript
                            key={listener.id}
                            listener={listener}
                            onEdit={this._triggerDialog(false, listener.id)}
                        />
                    )}
                </div>
            </div>
        );
    }
}


@connect(
    _state => { return {}; },
    ScriptActionCreators
)
class RestScript extends React.Component {
    static propTypes = {
        script: PropTypes.object.isRequired, //todo: model
        onEdit: PropTypes.func.isRequired,
        deleteScript: PropTypes.func.isRequired
    };

    _delete = () => {
        const script = this.props.script;

        // eslint-disable-next-line no-restricted-globals
        if (confirm(`Are you sure you want to delete "${script.name}"?`)) {
            restService.deleteScript(script.id).then(() => this.props.deleteScript(script.id));
        }
    };

    render() {
        const {script, onEdit} = this.props;

        return <div className="flex-column">
            <Script
                script={{
                    id: script.uuid,
                    name: script.name,
                    inline: true,
                    scriptBody: script.script
                }}

                withChangelog={false}
                editable={true}
                onEdit={onEdit}
                onDelete={this._delete}
            >
                {/*todo: rest info*/}
            </Script>
        </div>;
    }
}
