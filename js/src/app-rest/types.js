//@flow
import type {HttpMethod, ScriptEntity} from '../common/types';


export type RestScriptType = ScriptEntity & {
    uuid: string,
    methods: $ReadOnlyArray<HttpMethod>,
    groups: $ReadOnlyArray<string>
};
