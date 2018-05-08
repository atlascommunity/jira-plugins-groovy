//@flow
// eslint-disable-next-line import/no-extraneous-dependencies
import i18n from 'external-i18n';

import type {I18nFunction} from '../common/types';


export const ConsoleMessages: {[string]: string, executedIn: I18nFunction} =  {
    execute: i18n.console.execute,
    //todo: move to common
    executedIn: i18n.console.executedIn
};
