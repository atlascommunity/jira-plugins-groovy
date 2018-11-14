//@flow
import React, {Fragment} from 'react';

import memoize from 'lodash/memoize';

import Modal from '@atlaskit/modal-dialog';
import {DynamicTableStateless} from '@atlaskit/dynamic-table';

import type {ClassDoc, TypeDoc} from './types';

import './ClassDocViewer.less';


type Props = {
    classDoc: ClassDoc,
    onClose: () => void
};

function renderType(type: TypeDoc) {
    if (!type) {
        return '???';
    }
    if (type.link) {
        return <span dangerouslySetInnerHTML={{__html: type.link}}/>;
    }
    return type.className;
}

export class ClassDocViewer extends React.PureComponent<Props> {
    render() {
        const {classDoc, onClose} = this.props;

        return (
            <Modal
                width="large"
                autoFocus={false}

                heading={classDoc.className}
                onClose={onClose}
            >
                {classDoc.description &&
                    <div className="myGroovyDoc">
                        <div dangerouslySetInnerHTML={{__html: classDoc.description}}/>
                    </div>
                }
                <DynamicTableStateless
                    head={{
                        cells: [
                            {
                                content: 'Return Type'
                            },
                            {
                                content: 'Name and description'
                            }
                        ]
                    }}
                    rows={classDoc.methods
                        ? classDoc.methods.map(method =>
                            ({
                                cells: [
                                    {
                                        content: (
                                            <code className="myGroovyDoc">
                                                {renderType(method.returnType)}
                                            </code>
                                        )
                                    },
                                    {
                                        content: (
                                            <div className="myGroovyDoc">
                                                <code>
                                                    {method.name}
                                                    {'('}
                                                    {method.parameters.map((parameter, i: number) =>
                                                        <Fragment>
                                                            {renderType(parameter.type)}
                                                            {' '}
                                                            {parameter.name}
                                                            {(i !== (method.parameters.length-1)) ? ', ' : ''}
                                                        </Fragment>
                                                    )}
                                                    {')'}
                                                </code>
                                                {method.description && <div dangerouslySetInnerHTML={{__html: method.description}}/>}
                                            </div>
                                        )
                                    }
                                ]
                            })
                        )
                        : []
                    }
                />
            </Modal>
        );
    }
}
