grammar AggregateExpressionQuery;

DOT: '.';
COMMA: ',';
LEFT_BRACKET: '(';
RIGHT_BRACKET: ')';

PLUS: '+';
MINUS: '-';
DIV: '/';
MULT: '*';

STRING: DQSTRING | SQSTRING;

DQSTRING: '"' ( '\\"' | . )*? '"';
SQSTRING: '\'' ( '\\\'' | . )*? '\'';

WS : [ \r\t\n]+ -> skip ;

NUMBER: '0'..'9'+;

UQSTRING: ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')+;


variable: UQSTRING;

method_parameter_value: STRING | NUMBER;

method_parameters: method_parameter_value (COMMA method_parameter_value)*;

method_name: UQSTRING;

method_call: method_name LEFT_BRACKET method_parameters? RIGHT_BRACKET;

statement: variable DOT method_call;

arithmetic_operation: PLUS | MINUS | DIV | MULT;

aggregate_expression:
    LEFT_BRACKET aggregate_expression arithmetic_operation aggregate_expression RIGHT_BRACKET |
    aggregate_expression arithmetic_operation aggregate_expression |
    LEFT_BRACKET statement RIGHT_BRACKET |
    statement;
