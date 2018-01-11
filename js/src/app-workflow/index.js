import React from 'react';
import ReactDOM from 'react-dom';

import {FormEditor} from './FormEditor';
import {RegistryPicker} from './RegistryPicker';

import {Script} from '../common/Script';

import '../flex.less';


function noop() {}

window.define('mailru/groovy/renderRegistryScript', () => {
    return (element, id, name, source) => {
        ReactDOM.render(
            <Script
                withChangelog={false}
                editable={false}

                script={{
                    id: id,
                    name: name,
                    scriptBody: source
                }}

                onEdit={noop}
                onDelete={noop}
            />,
            element
        );
    };
});

window.define('mailru/groovy/renderInlineScript', () => {
    return (element, id, name, source) => {
        ReactDOM.render(
            <Script
                withChangelog={false}
                editable={false}

                script={{
                    id: id,
                    name: name || 'Inline script',
                    scriptBody: source,
                    inline: true
                }}

                onEdit={noop}
                onDelete={noop}
            />,
            element
        );
    };
});

window.define('mailru/groovy/renderEditor', () => {
    return (element, fieldName, initialValue) => {
        ReactDOM.render(
            <FormEditor fieldName={fieldName} initialValue={initialValue}/>,
            element
        );
    };
});

window.define('mailru/groovy/renderRegistryPicker', () => {
    return (element, fieldName, initialValue) => {
        ReactDOM.render(
            <RegistryPicker initialValue={initialValue} fieldName={fieldName}/>,
            element
        );
    };
});
