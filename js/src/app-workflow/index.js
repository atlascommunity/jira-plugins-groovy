import React from 'react';
import ReactDOM from 'react-dom';

import Avatar from '@atlaskit/avatar';

// eslint-disable-next-line import/no-extraneous-dependencies
import define from 'extDefine';

import {FormEditor} from './FormEditor';
import {RegistryPicker} from './RegistryPicker';

import {Script, ScriptParameters} from '../common/Script';

import {ErrorMessages} from '../i18n/common.i18n';

import '../flex.less';
import './workflow.less';


function ScriptParamValue({value, param}) {
    if (value === null || value === undefined) {
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
        case 'BOOLEAN':
            return value.toString();
        default:
            return <div>{'Unsupported type'}</div>;
    }
}

define('mailru/groovy/renderRegistryScript', () => {
    return (element, id, name, source, errorCount, params, paramValues) => {
        ReactDOM.render(
            <Script
                withChangelog={false}

                script={{
                    id: id,
                    name: name,
                    scriptBody: source,
                    errorCount
                }}
            >
                {params &&
                    <ScriptParameters
                        params={params.map(param => {
                            return {
                                label: param.displayName,
                                value: <ScriptParamValue value={paramValues[param.name]} param={param}/>
                            };
                        })}
                    />
                }
            </Script>,
            element
        );
    };
});

define('mailru/groovy/renderInlineScript', () => {
    return (element, id, name, source, errorCount) => {
        ReactDOM.render(
            <Script
                withChangelog={false}
                editable={false}
                deletable={false}

                script={{
                    id: id,
                    name: name || 'Inline script',
                    scriptBody: source,
                    inline: true,
                    errorCount
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
    return (element, fieldName, type, scriptId, values) => {
        ReactDOM.render(
            <RegistryPicker type={type} scriptId={scriptId} values={values} fieldName={fieldName}/>,
            element
        );
    };
});
