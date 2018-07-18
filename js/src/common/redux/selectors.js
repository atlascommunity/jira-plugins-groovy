//@flow
import {createSelector} from 'reselect';


export const orderedItemsSelector = () =>
    createSelector(
        [state => state.items],
        (items) => [...(items || [])].sort((a, b) => a.name.localeCompare(b.name, undefined, {sensitivity: 'base'}))
    );

export const createItemSelector = () =>
    createSelector(
        [
            state => state.items,
            (_state, props) => props.id
        ],
        (items, id) => items.find(it => it.id === id)
    );
