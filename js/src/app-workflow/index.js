import React from 'react';
import ReactDOM from 'react-dom';

import Avatar from 'aui-react/lib/AUIAvatar';

// eslint-disable-next-line import/no-extraneous-dependencies
import define from 'extDefine';

import {FormEditor} from './FormEditor';
import {RegistryPicker} from './RegistryPicker';

import {Script} from '../common/Script';
import {StaticField} from '../common/StaticField';

import '../flex.less';


function noop() {}

function ScriptParamValue({value, param}) {
    if (!value) {
        return <strong style={{color: 'red'}}>No value</strong>;
    }
    switch (param.paramType) {
        case 'USER':
            return <div>
                <Avatar size="xsmall" src={value.avatarUrl}/>{' '}{value.label}
            </div>;
        case 'GROUP':
            return value.label;
        case 'CUSTOM_FIELD':
            return value.label;
        case 'STRING':
        case 'TEXT':
        case 'LONG':
        case 'DOUBLE':
            return value;
        default:
            return <div>{'Unsupported type'}</div>;
    }
}

define('mailru/groovy/renderRegistryScript', () => {
    return (element, id, name, source, params, paramValues) => {
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
            >
                {params && <form className="aui" style={{margin: 0}}>
                    {params.map(param =>
                        <div className="field-group" key={param.name}>
                            <label>
                                {param.displayName}{':'}
                            </label>
                            <StaticField>
                                <ScriptParamValue value={paramValues[param.name]} param={param}/>
                            </StaticField>
                        </div>
                    )}
                </form>}
            </Script>,
            element
        );
    };
});

define('mailru/groovy/renderInlineScript', () => {
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

define('mailru/groovy/renderEditor', () => {
    return (element, fieldName, initialValue) => {
        ReactDOM.render(
            <FormEditor fieldName={fieldName} initialValue={initialValue}/>,
            element
        );
    };
});

define('mailru/groovy/renderRegistryPicker', () => {
    return (element, fieldName, scriptId, values) => {
        ReactDOM.render(
            <RegistryPicker scriptId={scriptId} values={values} fieldName={fieldName}/>,
            element
        );
    };
});
