//@flow
import {createSelector} from 'reselect';

import type {NamedItemType} from './actions';


export const orderedItemsSelector = () =>
    createSelector(
        [
            state => state.items,
            state => state.filter,
        ],
        (items: $ReadOnlyArray<NamedItemType>, rawFilter: string): $ReadOnlyArray<NamedItemType> => {
            const filter = rawFilter.toLocaleLowerCase();

            return [...(items || [])]
                .filter((script: NamedItemType): boolean => {
                    let matchesFilter: boolean = true;
                    if (filter.length >= 2) {
                        matchesFilter = script.name.toLocaleLowerCase().includes(filter);
                    }
                    return matchesFilter;
                })
                .sort((a, b) => a.name.localeCompare(b.name, undefined, {sensitivity: 'base'}));
        }
    );

export const createItemSelector = () =>
    createSelector(
        [
            state => state.items,
            (_state, props) => props.id
        ],
        (items, id) => items.find(it => it.id === id)
    );
