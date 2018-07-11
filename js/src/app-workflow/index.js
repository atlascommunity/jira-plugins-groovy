//@flow
import React, {type Node} from 'react';
import ReactDOM from 'react-dom';

import Avatar from '@atlaskit/avatar';
import Button from '@atlaskit/button';

import EditFilledIcon from '@atlaskit/icon/glyph/edit-filled';

// eslint-disable-next-line import/no-extraneous-dependencies
import define from 'extDefine';

import {FormEditor} from './FormEditor';
import {RegistryPicker} from './RegistryPicker';
import type {ParamType, ScriptType} from './types';

import Script, {ScriptParameters} from '../common/script';
import type {ScriptParam} from '../common/script/ScriptParameters';

import {ErrorMessages} from '../i18n/common.i18n';

import '../flex.less';
import './workflow.less';
import {registryService} from '../service/services';
import {getBaseUrl} from '../service/ajaxHelper';
import {RegistryMessages} from '../i18n/registry.i18n';


export type ScriptParamValueProps = {
    value: any,
    param: ParamType
};

function ScriptParamValue({value, param}: ScriptParamValueProps): Node {
    if (value === null || value === undefined) {
        return <strong style={!param.optional ? {color: 'red'} : undefined}>{ErrorMessages.noValue}</strong>;
    }
    switch (param.paramType) {
        case 'USER':
            return <div>
                <Avatar size="xsmall" src={value.avatarUrl}/>{' '}{value.label}
            </div>;
        case 'MULTI_USER':
            return value.map(item =>
                <div>
                    <Avatar key={item.value} size="xsmall" src={item.avatarUrl}/>{' '}{item.label}
                </div>
            );
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
        case 'SCRIPT':
            return (
                <pre>
                    {value}
                </pre>
            );
        default:
            return <div>{'Unsupported type'}</div>;
    }
}

define('mailru/groovy/renderRegistryScript', (): any => {
    return (element: Element, id: number, name: string, source: string, description: string, errorCount?: number, warningCount?: number, params: Array<ParamType>, paramValues: {[string]: any}) => {
        ReactDOM.render(
            <Script
                withChangelog={true}
                changelogsLoader={() => registryService.getScriptChangelogs(id)}

                script={{
                    id: id,
                    name: name,
                    scriptBody: source,
                    description, errorCount, warningCount
                }}

                additionalButtons={[
                    <Button
                        key="edit"
                        appearance="subtle"
                        iconBefore={<EditFilledIcon label=""/>}

                        href={`${getBaseUrl()}/plugins/servlet/my-groovy/registry/script/edit/${id}`}
                    >
                        {RegistryMessages.editScript}
                    </Button>
                ]}
            >
                {params &&
                    <ScriptParameters
                        params={params.map((param: ParamType): ScriptParam => {
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

define('mailru/groovy/renderInlineScript', (): any => {
    return (element: Element, id: string, name: string, source: string, errorCount?: number) => {
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

define('mailru/groovy/renderEditor', (): any => {
    return (element: Element, fieldName: string, initialValue: ?string) => {
        ReactDOM.render(
            <FormEditor fieldName={fieldName} initialValue={initialValue}/>,
            element
        );
    };
});

define('mailru/groovy/renderRegistryPicker', (): any => {
    return (element: Element, fieldName: string, type: ScriptType, scriptId: ?number, values: {[string]: any}) => {
        ReactDOM.render(
            <RegistryPicker type={type} scriptId={scriptId} values={values} fieldName={fieldName}/>,
            element
        );
    };
});
