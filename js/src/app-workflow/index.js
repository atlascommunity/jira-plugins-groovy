import React from 'react';
import ReactDOM from 'react-dom';

import Avatar from '@atlaskit/avatar';

// eslint-disable-next-line import/no-extraneous-dependencies
import define from 'extDefine';

import {FormEditor} from './FormEditor';
import {RegistryPicker} from './RegistryPicker';

import {Script} from '../common/Script';
import {StaticFieldValue} from '../common/StaticField';

import {ErrorMessages} from '../i18n/common.i18n';

import '../flex.less';
import './workflow.less';


function ScriptParamValue({value, param}) {
    if (!value) {
        return <strong style={{color: 'red'}}>{ErrorMessages.noValue}</strong>;
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

                script={{
                    id: id,
                    name: name,
                    scriptBody: source
                }}
            >
                {params && <form className="aui" style={{margin: 0}}>
                    {params.map(param =>
                        <div className="field-group" key={param.name}>
                            <label>
                                {param.displayName}{':'}
                            </label>
                            <StaticFieldValue>
                                <ScriptParamValue value={paramValues[param.name]} param={param}/>
                            </StaticFieldValue>
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
                deletable={false}

                script={{
                    id: id,
                    name: name || 'Inline script',
                    scriptBody: source,
                    inline: true
                }}
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
