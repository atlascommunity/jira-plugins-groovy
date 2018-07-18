grammar DateComparisonQuery;

operation_expr: LT | GT | GTE | LTE | EQ;
date_field_expr: UQSTRING | STRING;

duration: DURATION;

date_expr: date_field_expr (PLUS duration)?;

date_comparison_query: date_expr operation_expr date_expr EOF;

LT: '<';
GT: '>';
GTE: '>=';
LTE: '<=';
EQ: '=';
PLUS: '+';

SHORT_DAYS: 'd';
SHORT_WEEKS: 'w';
NUMBER: '0'..'9'+;

DURATION_UNIT: SHORT_DAYS | SHORT_WEEKS;

DURATION: NUMBER DURATION_UNIT;

STRING: DQSTRING | SQSTRING;

DQSTRING: '"' ( '\\"' | . )*? '"';
SQSTRING: '\'' ( '\\\'' | . )*? '\'';

WS : [ \r\t\n]+ -> skip ;

UQSTRING: ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')+;
