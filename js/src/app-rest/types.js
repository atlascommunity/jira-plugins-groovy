//@flow
import type {ScriptEntity} from '../common/types';


export type RestScriptType = ScriptEntity & {
    uuid: string,
    methods: Array<string>,
    groups: Array<string>
};
