//@flow
import {createSelector} from 'reselect';


export const fieldConfigSelectorFactory = (): * => {
    return createSelector(
        [state => state.items, (_state, props) => props.id],
        (items, id) => items.find(it => it.id === id)
    );
};
