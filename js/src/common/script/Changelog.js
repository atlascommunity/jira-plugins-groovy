//@flow
import React, {type Node} from 'react';

import reactStringReplace from 'react-string-replace';

import Avatar from '@atlaskit/avatar';
import Tooltip from '@atlaskit/tooltip';
import Badge from '@atlaskit/badge';
import {colors} from '@atlaskit/theme';

import type {ChangelogType, IssueReference} from './types';

import type {VoidCallback} from '../types';

import {CommonMessages} from '../../i18n/common.i18n';

import {getBaseUrl} from '../../service';


type ChangelogProps = {
    changelogs: $ReadOnlyArray<ChangelogType>,
    switchToCurrent: VoidCallback,
    switchToChangelog: (ChangelogType) => () => void
};

export function Changelog({changelogs, switchToCurrent, switchToChangelog}: ChangelogProps): Node {
    return (
        <div className="scriptChangelogs" style={{width: '150px'}}>
            <div key="current" className="scriptChangelog" onClick={switchToCurrent}>
                <div className="changelogContent">
                    <strong>{CommonMessages.currentVersion}</strong>
                </div>
            </div>
            {changelogs && changelogs.map(changelog =>
                <div key={changelog.id} className="scriptChangelog" onClick={switchToChangelog(changelog)}>
                    <div className="changelogContent">
                        <Tooltip content={changelog.author.displayName}>
                            <div className="flex-row">
                                <Avatar src={changelog.author.avatarUrl} size="xsmall" appearance="square" borderColor="transparent"/>
                                <span className="author">
                                    {changelog.author.displayName}
                                </span>
                            </div>
                        </Tooltip>
                        <div className="date">
                            {changelog.date}
                        </div>
                        <div>
                            <ChangelogComment
                                text={changelog.comment}
                                issueReferences={changelog.issueReferences}
                            />
                        </div>
                        {(changelog.warnings > 0 || changelog.errors > 0)
                            ? (
                                <div className="flex-row">
                                    {(changelog.warnings > 0) ? <Badge value={changelog.warnings} appearance={{ backgroundColor: colors.Y400, textColor: colors.N0 }}/> : undefined}
                                    {(changelog.errors > 0) ? <Badge value={changelog.errors} appearance="important"/> : undefined}
                                </div>
                            )
                            : undefined
                        }
                    </div>
                </div>
            )}
        </div>
    );
}

type ChangelogCommentProps = {
    text: string,
    issueReferences: Array<IssueReference>
};

function ChangelogComment({text, issueReferences}: ChangelogCommentProps): Node {
    if (!(issueReferences && issueReferences.length)) {
        return <p>{text}</p>;
    }

    return (
        <p>
            {reactStringReplace(
                text, /([A-Z0-9a-z]{1,10}-\d+)/g,
                (issueKey: string, i: number): Node => {
                    const issueReference = issueReferences.find(ref => ref.key === issueKey);

                    if (issueReference) {
                        return (
                            <Tooltip
                                key={`${issueKey}-${i}`}
                                tag="span"
                                content={issueReference.summary}
                            >
                                <a href={`${getBaseUrl()}/browse/${issueKey}`}>{issueKey}</a>
                            </Tooltip>
                        );
                    } else {
                        return issueKey;
                    }
                }
            )}
        </p>
    );
}
