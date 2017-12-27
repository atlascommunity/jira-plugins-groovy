import React from 'react';
import PropTypes from 'prop-types';

import Button from 'aui-react/lib/AUIButton';

import {ConditionModel, ListenerModel} from '../model/listener.model';
import {ListenerMessages} from '../i18n/listener.i18n';
import {Script} from '../common/Script';


//todo: redux
export class ListenerRegistry extends React.Component {
    static propTypes = {
        listeners: PropTypes.arrayOf(ListenerModel).isRequired,
        triggerDialog: PropTypes.func.isRequired
    };

    _triggerDialog = (isNew, id) => () => this.props.triggerDialog(isNew, id);

    render() {
        return (
            <div className="flex-column">
                <div>
                    <Button icon="add" type="primary" onClick={this._triggerDialog(true)}>
                        {ListenerMessages.addListener}
                    </Button>
                </div>
                <div className="flex-column">
                    {this.props.listeners.map(listener =>
                        <Listener
                            key={listener.id}
                            listener={listener}
                            onEdit={this._triggerDialog(false, listener.id)}
                            onDelete={() => console.log('delete') /* todo */}
                        />
                    )}
                </div>
            </div>
        );
    }
}

class Listener extends React.Component {
    static propTypes = {
        listener: ListenerModel.isRequired,
        onEdit: PropTypes.func.isRequired,
        onDelete: PropTypes.func.isRequired
    };

    render() {
        const {listener, onEdit, onDelete} = this.props;

        return <div className="flex-column">
            <Script
                script={{
                    id: listener.uuid,
                    name: listener.name,
                    inline: true,
                    scriptBody: listener.script
                }}

                withChangelog={false}
                editable={true}
                onEdit={onEdit}
                onDelete={onDelete}
            >
                <Condition condition={listener.condition}/>
            </Script>
        </div>;
    }
}

//todo
class Condition extends React.Component {
    static propTypes = {
        condition: ConditionModel.isRequired
    };

    render() {
        return <div>{JSON.stringify(this.props.condition)}</div>;
    }
}
