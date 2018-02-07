import React from 'react';
import PropTypes from 'prop-types';

import Dialog from 'aui-react/lib/AUIDialog';
import Button from 'aui-react/lib/AUIButton';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {ExecutionModel} from '../model/execution.model';
import {StaticFieldValue} from '../common/StaticField';


export class ExecutionDialog extends React.Component {
    static propTypes = {
        onClose: PropTypes.func.isRequired,
        execution: ExecutionModel.isRequired
    };

    render() {
        const {onClose, execution} = this.props;

        const bindings = execution.extraParams ? JSON.parse(execution.extraParams) : null;

        return <Dialog
            size="xlarge"
            titleContent="Execution info" //todo: i18n
            onClose={onClose}
            footerActionContent={[
                <Button key="close" type="link" onClick={onClose}>{CommonMessages.cancel}</Button>
            ]}
            type="modal"
            styles={{zIndex: '3000'}}
        >
            <form className="aui">
                <div className="field-group">
                    <label>
                        {FieldMessages.date}{':'}
                    </label>
                    <StaticFieldValue>
                        {execution.date}
                    </StaticFieldValue>
                </div>
                <div className="field-group">
                    <label>
                        {FieldMessages.time}{':'}
                    </label>
                    <StaticFieldValue>
                        {execution.time} {' ms'}
                    </StaticFieldValue>
                </div>
                <div className="field-group">
                    <label>
                        {FieldMessages.successful}{':'}
                    </label>
                    <StaticFieldValue>
                        {execution.success ? 'yes' : 'no'}
                    </StaticFieldValue>
                </div>
                {bindings &&
                    <div className="field-group">
                        <label>
                            {FieldMessages.bindings}{':'}
                        </label>
                        <StaticFieldValue>
                            {Object.keys(bindings).map(key =>
                                <div className="field-group" key={key}>
                                    <label>
                                        {key}
                                    </label>
                                    <StaticFieldValue>
                                        <pre>
                                            {bindings[key]}
                                        </pre>
                                    </StaticFieldValue>
                                </div>
                            )}
                        </StaticFieldValue>
                    </div>
                }
                {execution.error &&
                    <div className="field-group">
                        <label>
                            {CommonMessages.error}{':'}
                            </label>
                        <StaticFieldValue>
                            <pre>
                                {execution.error}
                            </pre>
                        </StaticFieldValue>
                    </div>
                }
            </form>
        </Dialog>;
    }
}
