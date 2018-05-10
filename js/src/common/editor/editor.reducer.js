//@flow
import {preferenceService} from '../../service/services';


const SWITCH_THEME = 'SWITCH_THEME';

export const EditorActionCreators = {
    switchTheme: function(isLight: boolean): * {
        return {
            type: SWITCH_THEME,
            isLight: isLight
        };
    }
};

type State = {
    isLight: boolean
};

export function editorReducer(state: State, action: typeof EditorActionCreators.switchTheme): State {
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

    return state;
}

export function subscribeToChanges(store: any, stateKey: string) {
    store.subscribe(() => {
        preferenceService.put('ru.mail.groovy.isLight', store.state[stateKey].isLight);
    });
}
