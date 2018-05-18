//@flow
import React, {type Node} from 'react';

import {connect} from 'react-redux';

import Flag, {FlagGroup} from '@atlaskit/flag';
import Spinner from '@atlaskit/spinner';


type Props = {
    ready: boolean
};

class UsageStatusFlagInternal extends React.PureComponent<Props> {
    render(): Node {
        const {ready} = this.props;

        if (ready) {
            return null;
        }

        return (
            <FlagGroup>
                <Flag
                    id="script-usage"
                    icon={<Spinner invertColor={true}/>}
                    title="Collecting script usage info"
                    appearance="info"
                />
            </FlagGroup>
        );
    }
}

export const UsageStatusFlag = connect(
    state => ({
        ready: state.scriptUsage.ready
    })
)(UsageStatusFlagInternal);
