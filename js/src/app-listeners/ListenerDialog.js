//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';

import {Record} from 'immutable';
import type {RecordOf, RecordFactory} from 'immutable';

import ModalDialog from '@atlaskit/modal-dialog';
import {FieldTextStateless} from '@atlaskit/field-text';
import {FieldTextAreaStateless} from '@atlaskit/field-text-area';

import {ConditionPicker} from './ConditionPicker';

import type {ListenerType, ConditionInputType} from './types';

import {ListenerMessages} from '../i18n/listener.i18n';
import {CommonMessages, DialogMessages, FieldMessages} from '../i18n/common.i18n';

import {listenerService} from '../service/services';
import {getMarkers} from '../common/error';
import {Bindings, ReturnTypes} from '../common/bindings';
import {EditorField} from '../common/ak/EditorField';
import {ErrorMessage} from '../common/ak/messages';
import {addItem, updateItem} from '../common/redux';
import type {FullDialogComponentProps} from '../common/script-list/types';
import type {BindingType} from '../common/editor/types';
import {extractShortClassName} from '../common/classNames';


//AbstractProjectEvent
//ProjectCategoryChangeEvent
//AbstractVersionEvent
//AbstractProjectComponentEvent
//DirectoryEvent
//AbstractCustomFieldEvent
//AbstractWorklogEvent
//IndexEvent
//AbstractRemoteIssueLinkEvent
//IssueWatcherAddedEvent
//IssueWatcherDeletedEvent

const issueEventBindings = [Bindings.issueEvent];
const returnTypes = [ReturnTypes.void];

type Form = {
    name: string,
    description: string,
    comment: string,
    scriptBody: string,
    condition: ConditionInputType
};

type FormField = $Keys<Form>;

const makeForm: RecordFactory<Form> = Record({
    name: '',
    description: '',
    comment: '',
    scriptBody: '',
    condition: {
        type: null,
        typeIds: [],
        projectIds: [],
        className: null
    }
});

type Props = FullDialogComponentProps & {
    addItem: typeof addItem,
    updateItem: typeof updateItem
};

type State = {
    ready: boolean,
    values: RecordOf<Form>,
    listener: ?ListenerType,
    error: *
};

class ListenerDialogInternal extends React.PureComponent<Props, State> {
    state = {
        ready: false,
        values: makeForm(),
        listener: null,
        error: null
    };

    componentWillReceiveProps(nextProps: Props) {
        this._init(nextProps);
    }

    componentDidMount() {
        this._init(this.props);
    }

    _init = ({isNew, id}: Props) => {
        if (!isNew && id) {
            this.setState({
                ready: false,
                values: makeForm()
            });

            listenerService
                .getListener(id)
                .then((listener: ListenerType) => {
                    this.setState({
                        values: makeForm({
                            name: listener.name,
                            description: listener.description || '',
                            scriptBody: listener.scriptBody,
                            //$FlowFixMe todo
                            condition: listener.condition
                        }),
                        listener: listener,
                        ready: true
                    });
                });
        } else {
            this.setState({
                ready: true,
                values: makeForm(),
                listener: null
            });
        }
    };

    _handleError = (error: *) => {
        const {response} = error;

        if (response.status === 400) {
            this.setState({ error: response.data });
        } else {
            throw error;
        }
    };

    _onSubmit = () => {
        const {isNew, id, onClose, addItem, updateItem} = this.props;
        const data = this.state.values.toJS();

        if (!isNew && id) {
            listenerService
                .updateListener(id, data)
                .then(
                    (listener: ListenerType) => {
                        onClose();
                        updateItem(listener);
                    },
                    this._handleError
                );
        } else {
            listenerService
                .createListener(data)
                .then(
                    (listener: ListenerType) => {
                        onClose();
                        addItem(listener);
                    },
                    this._handleError
                );
        }
    };

    mutateValue = (field: FormField, value: any) =>
        this.setState((state: State): * => {
            return {
                values: state.values.set(field, value)
            };
        });

    _setTextValue = (field) => (event) => this.mutateValue(field, event.currentTarget.value);

    _setObjectValue = (field) => (value) => this.mutateValue(field, value);

    render() {
        const {onClose, isNew} = this.props;
        const {ready, values, listener, error} = this.state;

        let body: ?Node = null;

        if (!ready) {
            body = <div>{DialogMessages.notReady}</div>;
        } else {
            let errorMessage: * = null;
            let errorField: ?string = null;

            let markers: * = null;

            if (error) {
                if (error.field === 'scriptBody' && Array.isArray(error.error)) {
                    const errors = error.error.filter(e => e);
                    markers = getMarkers(errors);
                    errorMessage = errors
                        .map(error => error.message)
                        .map(error => <p key={error}>{error}</p>);
                } else {
                    errorMessage = error.message;
                }
                errorField = error.field;
            }

            let bindings: ?Array<BindingType> = null;

            const condition = values.get('condition') || {};
            if (condition && condition.type) {
                if (condition.type === 'ISSUE') {
                    bindings = issueEventBindings;
                } else {
                    const className = condition.className;
                    if (className) {
                        const extractedName = extractShortClassName(className);
                        if (extractedName) {
                            bindings = [{
                                ...Bindings.event,
                                className: extractedName,
                                fullClassName: className
                            }];
                        }
                    }
                }
            }

            body =
                <div className="flex-column">
                    {error && !errorField && <ErrorMessage title={errorMessage}/>}

                    <FieldTextStateless
                        shouldFitContainer={true}
                        required={true}

                        isInvalid={errorField === 'name'}
                        invalidMessage={errorField === 'name' ? errorMessage : null}

                        label={FieldMessages.name}
                        value={values.get('name') || ''}
                        onChange={this._setTextValue('name')}
                    />

                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        minimumRows={5}

                        isInvalid={errorField === 'description'}
                        invalidMessage={errorField === 'description' ? errorMessage : null}

                        label={FieldMessages.description}
                        value={values.get('description') || ''}
                        onChange={this._setTextValue('description')}
                    />

                    <ConditionPicker value={condition} onChange={this._setObjectValue('condition')} error={error}/>
                    <EditorField
                        label={FieldMessages.scriptCode}
                        isRequired={true}

                        isInvalid={errorField === 'scriptBody'}
                        invalidMessage={errorField === 'scriptBody' ? errorMessage : null}
                        markers={markers}

                        bindings={bindings || undefined}
                        returnTypes={returnTypes}

                        value={values.get('scriptBody') || ''}
                        onChange={this._setObjectValue('scriptBody')}
                    />
                    <FieldTextAreaStateless
                        shouldFitContainer={true}
                        required={!isNew}

                        isInvalid={errorField === 'comment'}
                        invalidMessage={errorField === 'comment' ? errorMessage : null}

                        label={FieldMessages.comment}
                        value={values.get('comment') || ''}
                        onChange={this._setTextValue('comment')}
                    />
                </div>;
        }

        return <ModalDialog
            width="x-large"
            scrollBehavior="outside"

            isHeadingMultiline={false}
            heading={isNew ? ListenerMessages.addListener : `${ListenerMessages.editListener}: ${listener ? listener.name : ''}`}

            onClose={onClose}

            actions={[
                {
                    text: isNew ? CommonMessages.create : CommonMessages.update,
                    onClick: this._onSubmit
                },
                {
                    text: CommonMessages.cancel,
                    onClick: onClose
                }
            ]}
        >
            {body}
        </ModalDialog>;
    }
}

export const ListenerDialog = connect(
    null,
    { addItem, updateItem }
)(ListenerDialogInternal);
