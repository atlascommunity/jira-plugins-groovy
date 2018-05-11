//@flow

const LOAD_ITEMS = 'LOAD_ITEMS';

const DELETE_ITEM = 'DELETE_ITEM';
const UPDATE_ITEM = 'UPDATE_ITEM';
const ADD_ITEM = 'ADD_ITEM';

const ADD_WATCH = 'ADD_WATCH';
const REMOVE_WATCH = 'REMOVE_WATCH';

export type ItemType = {
    id: number
};

export type ItemListType = $ReadOnlyArray<ItemType>;
export type WatchesListType = $ReadOnlyArray<number>;

export type LoadAction = {
    type: typeof LOAD_ITEMS,
    items: ItemListType,
    watches: WatchesListType
};

export type DeleteItemAction = {
    type: typeof DELETE_ITEM,
    id: number
};

export type AddItemAction = {
    type: typeof ADD_ITEM,
    item: ItemType
};

export type UpdateItemAction = {
    type: typeof UPDATE_ITEM,
    item: ItemType
};

type Action = LoadAction | DeleteItemAction | AddItemAction | UpdateItemAction;

export function itemsReducer(state: ItemListType, action: Action): ItemListType {
    if (state === undefined) {
        return [];
    }

    switch (action.type) {
        case LOAD_ITEMS:
            return action.items;
        case DELETE_ITEM: {
            const {id} = action;
            return state.filter(it => it.id !== id);
        }
        case ADD_ITEM:
            return [...state, action.item];
        case UPDATE_ITEM: {
            const {item} = action;
            return state.map(it => (it.id === item.id) ? item : it);
        }
        default:
            return state;
    }
}

type WatcherAction<T> = {
    type: T,
    id: number
};

export type AddWatchAction = WatcherAction<typeof ADD_WATCH>;
export type RemoveWatchAction = WatcherAction<typeof REMOVE_WATCH>;

type WatcherActionType = LoadAction | DeleteItemAction | AddItemAction | AddWatchAction | RemoveWatchAction;

export function watchesReducer(state: WatchesListType, action: WatcherActionType): WatchesListType {
    if (state === undefined) {
        return [];
    }

    switch (action.type) {
        case LOAD_ITEMS:
            return action.watches;
        case DELETE_ITEM:
        case REMOVE_WATCH: {
            const {id} = action;
            return state.filter(it => it !== id);
        }
        case ADD_ITEM:
            return [...state, action.item.id];
        case ADD_WATCH:
            return [...state, action.id];
        default:
            return state;
    }
}

export function readinessReducer(state: boolean, action: LoadAction): boolean {
    if (state === undefined) {
        return false;
    }

    if (action.type === LOAD_ITEMS) {
        return true;
    }

    return state;
}

export type WholeObjectAction<Field, V> = {
    type: typeof LOAD_ITEMS,
    [Field]: V
};

export function wholeObjectReducerFactory<T, Field>(field: Field, defaultValue: T): (T, WholeObjectAction<Field, T>) => (?T) {
    return function(state: T, action: WholeObjectAction<Field, T>): ?T {
        if (state === undefined) {
            return defaultValue;
        }

        if (action.type === LOAD_ITEMS) {
            return action[field];
        }

        return state;
    };
}

export const ItemActionCreators = {
    loadItems: (items: ItemListType, watches: WatchesListType, extraFields?: {[string]: any}): LoadAction => {
        return {
            ...extraFields,
            type: LOAD_ITEMS, items, watches
        };
    },
    addItem: (item: ItemType): AddItemAction => {
        return {
            type: ADD_ITEM, item
        };
    },
    updateItem: (item: ItemType): UpdateItemAction => {
        return {
            type: UPDATE_ITEM, item
        };
    },
    deleteItem: (id: number): DeleteItemAction => {
        return {
            type: DELETE_ITEM, id: id
        };
    }
};

export const WatchActionCreators = {
    addWatch: (id: number): AddWatchAction => {
        return {
            type: ADD_WATCH, id
        };
    },
    removeWatch: (id: number): RemoveWatchAction => {
        return {
            type: REMOVE_WATCH, id
        };
    }
};
