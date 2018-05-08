//@flow
// eslint-disable-next-line import/no-extraneous-dependencies
import i18n from 'external-i18n';

import type {I18nFunction} from '../common/types';


export const ScriptFieldMessages: {[string]: string, scriptFor: I18nFunction} = {
    scriptFor: i18n.field.scriptFor,
    noFields: i18n.field.noFields
};
