//@flow
import * as React from 'react';

import {Draggable} from 'react-beautiful-dnd';

import Badge from '@atlaskit/badge';

import CodeIcon from '@atlaskit/icon/glyph/code';

import {RegistryScript, type PublicRegistryScriptProps} from './RegistryScript';


export class DraggableRegistryScript extends React.PureComponent<PublicRegistryScriptProps> {
    render(): React.Node {
        const {script} = this.props;

        return (
            <div className="DraggableScript">
                <Draggable draggableId={`${this.props.script.id}`} type="script">
                    {(provided) => (
                        //$FlowFixMe
                        <RegistryScript
                            title={
                                <div className="flex-grow flex-row" {...provided.dragHandleProps}>
                                    <div className="flex-vertical-middle flex-none">
                                        <CodeIcon label=""/>
                                    </div>
                                    {' '}
                                    <div className="flex-vertical-middle">
                                        <h3 title={script.name}>
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
