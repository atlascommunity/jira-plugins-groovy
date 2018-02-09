import {preferenceService} from '../../service/services';


const SWITCH_THEME = 'SWITCH_THEME';

export const EditorActionCreators = {
    switchTheme: function(isLight) {
        return {
            type: SWITCH_THEME,
            isLight: isLight
        };
    }
};

export function editorReducer(state, action) {
    if (state === undefined) {
        return {
            isLight: !(preferenceService.get('ru.mail.groovy.isLight') === 'false')
        };
    }

    if (action === SWITCH_THEME) {
        return {
            ...state,
            isLight: action.isLight
        };
    }
}

export function subscribeToChanges(store, stateKey) {
    store.subscribe(() => {
        preferenceService.put('ru.mail.groovy.isLight', store.state[stateKey].isLight);
    });
}
