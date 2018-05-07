//@flow
import type {ScriptEntity} from '../common/types';
import type {ParamType} from '../app-workflow/types';


export type AdminScriptType = ScriptEntity & {
    uuid: string,
    builtIn: boolean,
    builtInKey: ?string,
    params: ?Array<ParamType>
};

export type AdminScriptOutcomeType = {
    success: boolean,
    message: ?string
};
