import React from 'react';
import PropTypes from 'prop-types';

import Message from 'aui-react/lib/AUIMessage';


//todo: migrate all errors to this component
export function Error({error, thisField}) {
    if (!error || thisField !== error.field) {
        return (null);
    }

    const messages = error.message || error.messages.map(message => <div key={message}>{message}</div>);

    if (!thisField) {
        return (
            <Message type="error">
                {messages}
            </Message>
        );
    }
    return (
        <div className="error flex-column">
            {messages}
        </div>
    );
}

Error.propTypes = {
    error: PropTypes.shape({
        field: PropTypes.string,
        messages: PropTypes.arrayOf(PropTypes.string.isRequired)
    }),
    thisField: PropTypes.string
};
