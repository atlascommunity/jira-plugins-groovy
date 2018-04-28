//@flow
import * as React from 'react';

import Button from '@atlaskit/button';

import WatchIcon from '@atlaskit/icon/glyph/watch';
import WatchFilledIcon from '@atlaskit/icon/glyph/watch-filled';

import type {ScriptProps} from './Script';

import {watcherService} from '../../service/services';

import {CommonMessages} from '../../i18n/common.i18n';

import {WatchActionCreators} from '../redux';

import type {EntityType} from '../types';

import Script from './';


type Props = ScriptProps & {
    entityId: number,
    entityType: EntityType,
    addWatch: typeof WatchActionCreators.addWatch,
    removeWatch: typeof WatchActionCreators.removeWatch,
    watches: Array<number>
}

type State = {
    waitingWatch: boolean
}

export class WatchableScript extends React.Component<Props, State> {
    state = {
        waitingWatch: false
    };

    _toggleWatch = () => {
        const {entityId, entityType, watches, addWatch, removeWatch} = this.props;

        const isWatching = watches.includes(entityId);

        this.setState({ waitingWatch: true });

        const promise = isWatching ?
            watcherService.stopWatching(entityType, entityId) :
            watcherService.startWatching(entityType, entityId);

        promise.then(
            () => {
                (isWatching ? removeWatch : addWatch)(entityId);
                this.setState({ waitingWatch: false });
            },
            (error: any) => {
                this.setState({ waitingWatch: false });
                throw error;
            }
        );
    };

    render(): React.Node {
        const {watches, entityId, entityType, dropdownItems, additionalButtons, onDelete, ...props} = this.props;
        const {waitingWatch} = this.state;

        const dropdown = dropdownItems ? [...dropdownItems] : [];
        const buttons = additionalButtons ? [...additionalButtons] : [];

        if (onDelete) {
            dropdown.push({
                label: CommonMessages.delete,
                onClick: onDelete
            });
        }

        buttons.push(
            <Button
                key="watch"
                appearance="subtle"
                isDisabled={waitingWatch}
                iconBefore={watches.includes(entityId) ? <WatchFilledIcon label=""/> : <WatchIcon label=""/>}

                onClick={this._toggleWatch}
            />,
        );

        return (
            <Script additionalButtons={buttons} dropdownItems={dropdown} {...props}/>
        );
    }
}
