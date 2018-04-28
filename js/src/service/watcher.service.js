//@flow
import {ajaxDelete, ajaxGet, ajaxPost, getPluginBaseUrl} from './ajaxHelper';

import type {EntityType} from '../common/types';


export class WatcherService {
    getAllWatches(type: EntityType): Promise<Array<number>> {
        return ajaxGet(`${getPluginBaseUrl()}/watch/${type}/all`);
    }

    startWatching(type: EntityType, id: number): Promise<void> {
        return ajaxPost(`${getPluginBaseUrl()}/watch/${type}/${id}`);
    }

    stopWatching(type: EntityType, id: number): Promise<void> {
        return ajaxDelete(`${getPluginBaseUrl()}/watch/${type}/${id}`);
    }
}
