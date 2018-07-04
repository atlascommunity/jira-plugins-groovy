grammar CommentedQuery;

BY: 'by';
ON: 'on';
LIKE: 'like';
AFTER: 'after';
BEFORE: 'before';
IN_ROLE: 'inRole';
IN_GROUP: 'inGroup';
ROLE_LEVEL: 'roleLevel';
GROUP_LEVEL: 'groupLevel';

STRING: DQSTRING | SQSTRING;

DQSTRING: '"' ( '\\"' | . )*? '"';
SQSTRING: '\'' ( '\\\'' | . )*? '\'';

WS : [ \r\t\n]+ -> skip ;

UQSTRING: ('a'..'z' | 'A'..'Z' | '0'..'9' | '@' | '-' | '(' | ')')+;

str_expr: STRING;
date_expr: STRING | UQSTRING;
username_expr : STRING | UQSTRING;
role_expr: STRING | UQSTRING;
group_expr: STRING | UQSTRING;

date_field: AFTER | BEFORE | ON;
group_field: IN_GROUP | GROUP_LEVEL;
role_field: IN_ROLE | ROLE_LEVEL;

by_query: BY username_expr;
like_query: LIKE str_expr;
date_query: date_field date_expr;
group_query: group_field group_expr;
role_query: role_field role_expr;

query: like_query | by_query | date_query | group_query | role_query;

q: q q | query;

commented_query: q EOF;
