//@flow
import * as React from 'react';

import {Label} from '@atlaskit/field-base';


type ScriptParam = {
    label: string,
    value: React.Node
}

type ScriptParametersProps = {
    params: Array<ScriptParam>
};

export class ScriptParameters extends React.PureComponent<ScriptParametersProps> {
    render(): React.Node {
        const {params} = this.props;

        return (
            <div className="scriptParams">
                {params.map((param, i) =>
                    <div className="item" key={i}>
                        <div className="label">
                            <Label label={`${param.label}:`} isFirstChild={true}/>
                        </div>
                        <div className="value">
                            {param.value}
                        </div>
                    </div>
                )}
            </div>
        );
    }
}
