//@flow
import {createSelector} from 'reselect';

import groupBy from 'lodash/groupBy';

import type {FilterType, KeyedEntities, RegistryDirectoryType, RegistryScriptType, ScriptUsageType} from '../types';


export const filterSelector = (state: *) => state.filter;

function addChildren(
    dir: RegistryDirectoryType,
    dirsResult: KeyedEntities<RegistryDirectoryType>,
    scriptsResult: KeyedEntities<RegistryScriptType>,
    directories: $ReadOnlyArray<RegistryDirectoryType>,
    scripts: $ReadOnlyArray<RegistryScriptType>
) {
    scripts
        .filter(it => it.directoryId === dir.id)
        .forEach(it => scriptsResult[it.id] = it);

    directories
        .filter(it => it.parentId === dir.id)
        .forEach((it: RegistryDirectoryType) => {
            dirsResult[it.id] = it;
            addChildren(it, dirsResult, scriptsResult, directories, scripts);
        });
}

export const filteredSelector = createSelector(
    [
        state => state.scripts,
        state => state.directories,
        state => state.scriptUsage,
        filterSelector
    ],
    (
        scripts: KeyedEntities<RegistryScriptType>,
        directories: KeyedEntities<RegistryDirectoryType>,
        scriptUsage: ScriptUsageType,
        filter: FilterType
    ): * => {
        const nameFilter = filter.name.toLocaleLowerCase();

        if (nameFilter.length < 2 && !filter.onlyUnused) {
            return {scripts, directories, isForceOpen: false};
        }
        const scriptsResult = {};
        const dirsResult = {};

        //$FlowFixMe: Object.values returns mixed type, consider using es6 Map or immutablejs map
        const allScripts: $ReadOnlyArray<RegistryScriptType> = Object.values(scripts);
        allScripts
            .filter((script: RegistryScriptType): boolean => {
                let matchesUnused: boolean = true;
                let matchesFilter: boolean = true;
                if (filter.onlyUnused && scriptUsage.ready) {
                    matchesUnused = (scriptUsage.items[script.id] || 0) === 0;
                }
                if (nameFilter.length >= 2) {
                    matchesFilter = script.name.toLocaleLowerCase().includes(nameFilter);
                }
                return matchesUnused && matchesFilter;
            })
            .forEach((script: RegistryScriptType) => {
                scriptsResult[script.id] = script;
                //add all parents
                let directory: ?RegistryDirectoryType = directories[script.directoryId];
                while (directory != null) {
                    if (dirsResult[directory.id]) {
                        return;
                    }
                    dirsResult[directory.id] = directory;
                    if (directory.parentId) {
                        directory = directories[directory.parentId];
                    } else {
                        directory = null;
                    }
                }
            });

        if (!filter.onlyUnused && nameFilter) {
            //$FlowFixMe: Object.values returns mixed type, consider using es6 Map or immutablejs map
            const allDirectories: $ReadOnlyArray<RegistryDirectoryType> = Object.values(directories);
            allDirectories
                .filter((script: RegistryDirectoryType): boolean => {
                    let matchesFilter: boolean = true;
                    if (nameFilter.length >= 2) {
                        matchesFilter = script.name.toLocaleLowerCase().includes(nameFilter);
                    }
                    return matchesFilter;
                })
                .forEach((currentDirectory: RegistryDirectoryType) => {
                    //add all children
                    addChildren(currentDirectory, dirsResult, scriptsResult, allDirectories, allScripts);

                    //add all parents
                    let directory: ?RegistryDirectoryType = currentDirectory;
                    while (directory != null) {
                        if (dirsResult[directory.id]) {
                            return;
                        }
                        dirsResult[directory.id] = directory;
                        if (directory.parentId) {
                            directory = directories[directory.parentId];
                        } else {
                            directory = null;
                        }
                    }
                });
        }

        return {
            scripts: scriptsResult,
            directories: dirsResult,
            isForceOpen: (Object.keys(scriptsResult).length + Object.keys(dirsResult).length) <= 50
        };
    }
);

const groupedSelector = (parentKey) => (entities: *) => groupBy(Object.values(entities), parentKey);

const scriptsSelector = createSelector(
    [filteredSelector],
    (filtered) => filtered.scripts
);

const directoriesSelector = createSelector(
    [filteredSelector],
    (filtered) => filtered.directories
);

export const groupedScriptsSelector = createSelector(
    scriptsSelector,
    groupedSelector('directoryId')
);

export const groupedDirsSelector = createSelector(
    directoriesSelector,
    groupedSelector('parentId')
);

export const scriptSelectorFactory =
    () => createSelector(
        [
            state => state.scripts,
            (_state, props) => props.id
        ],
        (scripts: KeyedEntities<RegistryScriptType>, id: number) => scripts[id]
    );

