//@flow
import React from 'react';

import memoizeOne from 'memoize-one';
import {connect} from 'react-redux';

import {Draggable} from 'react-beautiful-dnd';

import Badge from '@atlaskit/badge';

import CodeIcon from '@atlaskit/icon/glyph/code';

import {RegistryScript, type PublicRegistryScriptProps} from './RegistryScript';
import type {ScriptUsageType} from './types';


type Props = {
    scriptUsage: ScriptUsageType
};

export class DraggableRegistryScriptInternal extends React.PureComponent<PublicRegistryScriptProps & Props> {
    render() {
        const {script, scriptUsage} = this.props;
        const isUsed = !scriptUsage.ready || ((scriptUsage.items[script.id] || 0) > 0);

        return (
            <div className="DraggableScript">
                <Draggable draggableId={`${this.props.script.id}`} type="script">
                    {(provided) => (
                        <RegistryScript
                            title={
                                <div className="flex-grow flex-row" {...provided.dragHandleProps}>
                                    <div className="flex-vertical-middle flex-none">
                                        <CodeIcon label=""/>
                                    </div>
                                    {' '}
                                    <div className="flex-vertical-middle">
                                        <h3 title={script.name} className={!isUsed ? 'muted-text' : undefined}>
                                            {script.name}
                                        </h3>
                                    </div>
                                    {!!script.errorCount &&
                                        <div className="flex-vertical-middle flex-none errorCount">
                                            <div>
                                                <Badge max={99} value={script.errorCount} appearance="important"/>
                                            </div>
                                        </div>
                                    }
                                </div>
                            }
                            wrapperProps={{ ...provided.draggableProps, ref: provided.innerRef }}
                            {...this.props}
                        />
                    )}
                </Draggable>
            </div>
        );
    }
}

export const DraggableRegistryScript = connect(
    memoizeOne(({scriptUsage}: *): * => {
        return {
            scriptUsage
        };
    })
)(DraggableRegistryScriptInternal);
