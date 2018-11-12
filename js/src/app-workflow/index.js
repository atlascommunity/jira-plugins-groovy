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
import {registryService, getBaseUrl} from '../service';
import {RegistryMessages} from '../i18n/registry.i18n';
import {EditorThemeContext} from '../common/editor';


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
            return (
                <div>
                    <Avatar size="xsmall" src={value.avatarUrl}/>{' '}{value.label}
                </div>
            );
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
    return (element: Element, id: number, uuid: ?string, name: string, source: string, description: string, errorCount?: number, warningCount?: number, params: Array<ParamType>, paramValues: {[string]: any}) => {
        ReactDOM.render(
            <EditorThemeContext>
                <Script
                    withChangelog={true}
                    // eslint-disable-next-line react/jsx-no-bind
                    changelogsLoader={() => registryService.getScriptChangelogs(id)}

                    script={{
                        id: id,
                        name: name,
                        scriptBody: source,
                        description, errorCount, warningCount,
                        //$FlowFixMe
                        ...(uuid ? { inline: true, id: uuid } : {})
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
                </Script>
            </EditorThemeContext>,
            element
        );
    };
});

define('mailru/groovy/renderInlineScript', (): any => {
    return (element: Element, id: string, name: string, source: string, errorCount?: number) => {
        ReactDOM.render(
            <EditorThemeContext>
                <Script
                    withChangelog={false}

                    script={{
                        id: id,
                        name: name || 'Inline script',
                        scriptBody: source,
                        inline: true,
                        errorCount
                    }}
                />
            </EditorThemeContext>,
            element
        );
    };
});

define('mailru/groovy/renderEditor', (): any => {
    return (element: Element, fieldName: string, initialValue: ?string) => {
        ReactDOM.render(
            <EditorThemeContext>
                <FormEditor fieldName={fieldName} initialValue={initialValue}/>
            </EditorThemeContext>,
            element
        );
    };
});

define('mailru/groovy/renderRegistryPicker', (): any => {
    return (element: Element, fieldName: string, type: ScriptType, scriptId: ?number, values: {[string]: any}) => {
        ReactDOM.render(
            <EditorThemeContext>
                <RegistryPicker type={type} scriptId={scriptId} values={values} fieldName={fieldName}/>
            </EditorThemeContext>,
            element
        );
    };
});
