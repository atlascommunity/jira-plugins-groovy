import React from 'react';
import PropTypes from 'prop-types';

import ModalDialog from '@atlaskit/modal-dialog';

import {CommonMessages, FieldMessages} from '../i18n/common.i18n';
import {ExecutionModel} from '../model/execution.model';
import {StaticField} from '../common/ak/StaticField';


export class ExecutionDialog extends React.Component {
    static propTypes = {
        onClose: PropTypes.func.isRequired,
        execution: ExecutionModel.isRequired
    };

    render() {
        const {onClose, execution} = this.props;

        const bindings = execution.extraParams ? JSON.parse(execution.extraParams) : null;

        return <ModalDialog
            width="x-large"
            heading="Execution info" //todo: i18n
            onClose={onClose}
            actions={[
                {
                    text: CommonMessages.cancel,
                    onClick: onClose
                }
            ]}
        >
            <div className="flex-column">
                <StaticField label={FieldMessages.date}>
                    {execution.date}
                </StaticField>
                <StaticField label={FieldMessages.time}>
                    {execution.time}{' ms'}
                </StaticField>
                <StaticField label={FieldMessages.successful}>
                    {execution.success ? 'yes' : 'no'}
                </StaticField>
                {bindings &&
                    <div className="flex-column">
                        {Object.keys(bindings).map(key =>
                            <StaticField label={key} key={key}>
                                <pre style={{overflowX: 'auto'}}>
                                    {bindings[key]}
                                </pre>
                            </StaticField>
                        )}
                    </div>
                }
                {execution.error &&
                    <StaticField label={CommonMessages.error}>
                        <pre style={{overflowX: 'auto'}}>
                            {execution.error}
                        </pre>
                    </StaticField>
                }
            </div>
        </ModalDialog>;
    }
}
