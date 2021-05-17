package it.ru.mail.jira.plugins.groovy.jql;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Getter;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.junit.Test;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import java.util.HashSet;
import java.util.Set;


public class ExpressionFunctionIT {

    /*class AllVariableExpressionsVisitor extends ClassCodeVisitorSupport {
        @Getter
        private final Set<String> fields = new HashSet<>();

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            if (expression.getAccessedVariable() instanceof DynamicVariable) {
                fields.add(expression.getName());
            }
            super.visitVariableExpression(expression);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        public void clear() {
            fields.clear();
        }
    }

     class AllVariablesMemorizerExtension extends CompilationCustomizer {
        private final ClassCodeVisitorSupport visitor;

        public AllVariablesMemorizerExtension(ClassCodeVisitorSupport visitor) {
            super(CompilePhase.CANONICALIZATION);
            this.visitor = visitor;
        }

        @Override
        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
            classNode.visitContents(visitor);
        }
    }

    @Test
    public void testFindAllUsedFieldNames() {
        String testData1 = "reporter != assignee";
        String testData2 = "(field1 * 2 + 3/4 * (field1-field2)) < field3 + assignee";
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        AllVariableExpressionsVisitor visitor = new AllVariableExpressionsVisitor();
        compilerConfiguration.addCompilationCustomizers(new AllVariablesMemorizerExtension(visitor));

        GroovyClassLoader gcl = new GroovyClassLoader(ClassLoaderUtil.getCurrentPluginClassLoader(), compilerConfiguration);
        //gcl.parseClass(expressionsQueryStr);

        // TODO bindings for jql and dates short values like: wd, 2w, 3d etc...
        GroovyShell groovyShell = new GroovyShell(gcl, new Binding());
        gcl.parseClass(testData1);
        System.out.println(visitor.getFields());
        visitor.clear();

        gcl.parseClass(testData2);
        System.out.println(visitor.getFields());


    }*/

}

/*
@Ignore
public class ExpressionFunctionIT {
  private final SearchService searchService;

    private final SearchHandlerManager searchHandlerManager;
    private final CustomFieldManager customFieldManager;
    private final JqlQueryParser jqlQueryParser;
    private final SearchProvider searchProvider;

    class AllVariableExpressionsVisitor extends ClassCodeVisitorSupport {
        @Getter
        private Set<String> fields = new HashSet<>();

        public AllVariableExpressionsVisitor() {
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            if (expression.getAccessedVariable() instanceof DynamicVariable) {
                fields.add(expression.getName());
            }
            super.visitVariableExpression(expression);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }

    class AllVariablesMemorizerExtension extends CompilationCustomizer {
        private final ClassCodeVisitorSupport visitor;

        public AllVariablesMemorizerExtension(ClassCodeVisitorSupport visitor) {
            super(CompilePhase.CANONICALIZATION);
            this.visitor = visitor;
        }

        @Override
        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
            classNode.visitContents(visitor);
        }
    }


    @Test
    public void someTest1() throws JqlParseException {
        QueryCreationContext qcc;
        String scriptText = "(field1 * 2 + 3/4 * (field1-field2)) < field3 + assignee";

        AllVariableExpressionsVisitor visitor = new AllVariableExpressionsVisitor();
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(new AllVariablesMemorizerExtension(visitor));
        GroovyClassLoader gcl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), compilerConfiguration);
        Class aClass = gcl.parseClass(scriptText);
        System.out.println(visitor.getFields());
        Set<String> fields = visitor.getFields();
        Query query = jqlQueryParser.parseQuery("");
        Set<ClauseInformation> allClauseInfos = findAllClauseInfos(fields);
        Query nonEmptyFieldsQuery = buildNonEmptyFieldsQuery(query, allClauseInfos);
        searchProvider.search(nonEmptyFieldsQuery);

        *//*
         Query getQuery(QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause, Query limitingQuery) {
        def scriptText = operand.args[1]
        def unboundVariables = getUnboundVariables(scriptText)
        def scriptCfNameMap = getClauseInfosForVars(unboundVariables, queryCreationContext.applicationUser)

        def binding = getBinding(queryCreationContext)
        try {
            def shell = SecureShellUtil.getSecureShell(binding)

            def script = shell.parse(scriptText)
            def subquery = operand.args[0]

            def query = jqlQueryParser.parseQuery(subquery)

            def hitCollector = new ScriptHitCollector(script, unboundVariables, scriptCfNameMap)

            def nonEmptyQuery = getNonEmptyQuery(scriptCfNameMap, query)
            log.debug("Execute subquery: " + searchService.getJqlString(nonEmptyQuery))

            searchIssuesForDeterminedProjects(nonEmptyQuery, queryCreationContext, limitingQuery, hitCollector)

            return new ConstantScoreQuery(new IssueIdFilter(hitCollector.issueIds))
        }
        finally {
            removeFromMetaClassRegistry()
        }
    }
         *//*
    }

    public Set<ClauseInformation> findAllClauseInfos(Collection<String> fieldNames) {
        List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects();
        return fieldNames.stream().map(fieldName -> {
            Collection<ClauseHandler> clauseHandler = searchHandlerManager.getClauseHandler(fieldName);
            if (clauseHandler.size() > 0) {
                return clauseHandler.iterator().next().getInformation();
            }

            Optional<CustomField> cfOptional = customFieldObjects.stream().filter(cf -> isFieldEqualsCf(fieldName, cf)).findFirst();
            if (!cfOptional.isPresent())
                throw new RuntimeException("SOMETHING REALLY BAD HAPPENED");
            CustomField foundField = cfOptional.get();
            if (!searcherIsSupported(cfOptional.get()))
                throw new RuntimeException("SOMETHING REALLY BAD HAPPENED");
            return new SimpleFieldSearchConstants(foundField.getId(), OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.ALL);
        }).collect(Collectors.toSet());
    }

    private boolean isFieldEqualsCf(String fieldName, CustomField customField) {
        String lowerCasedFieldName = fieldName.toLowerCase();
        String lowerCasedCFName = customField.getName().replaceAll("\\s+", "").toLowerCase();
        return lowerCasedFieldName.equals(lowerCasedCFName) || lowerCasedCFName.equals(customField.getId());
    }

    private boolean searcherIsSupported(CustomField cf) {
        CustomFieldSearcher customFieldSearcher = cf.getCustomFieldSearcher();
        return customFieldSearcher instanceof ExactNumberSearcher ||
                customFieldSearcher instanceof NumberRangeSearcher ||
                customFieldSearcher instanceof DateTimeRangeSearcher ||
                customFieldSearcher instanceof DateRangeSearcher;
    }

    private Query buildNonEmptyFieldsQuery(Query query, Set<ClauseInformation> clauseInformations) {
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder(query.getWhereClause()).defaultAnd();
        clauseInformations.forEach(clauseInformation -> {
            if (clauseInformation.getFieldId() != null && clauseInformation.getFieldId().startsWith("customfield_")) {
                String cfId = clauseInformation.getFieldId().replaceAll("customfield_", "");
                jqlClauseBuilder.addClause(JqlQueryBuilder.newClauseBuilder().customField(Long.valueOf(cfId)).isNotEmpty().buildClause());
            } else {
                jqlClauseBuilder.addClause(JqlQueryBuilder.newClauseBuilder().field(clauseInformation.getJqlClauseNames().getPrimaryName()).isNotEmpty().buildClause());
            }
        });
        return jqlClauseBuilder.buildQuery();
    }


    @Test
    public void groovyShellScript() {
        String scriptText = "(field1 * 2 + 3/4 * (field1-field2)) < field3";
        GroovyShell groovyShell = new GroovyShell(new Binding(ImmutableMap.of("field1", 1, "field2", 2, "field3", 3)));
        Script script = groovyShell.parse(scriptText);

        System.out.println(script.getBinding().getVariables());
        //script.setProperty();
        //searchService.search(null, query, new PagerFilter())
        //System.out.println(groovyShell.evaluate());
    }

    @Test
    public void someTest() {
        String someTestString = "(field1 * 2 + 3/4 * (field1-field2)) < field3";
        ExampleExpressionsQueryLexer lexer = new ExampleExpressionsQueryLexer(CharStreams.fromString(someTestString));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        ExampleExpressionsQueryParser parser = new ExampleExpressionsQueryParser(tokenStream);

        MyListener listener = new MyListener(ImmutableMap.of("field1", "1", "field2", "2", "field3", "3"));

        parser.addParseListener(listener);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                System.out.println("Parsing error at " + charPositionInLine + ": " + msg);
            }
        });
        try {
            ExampleExpressionsQueryParser.ExContext ex = parser.ex();
            System.out.println("operands = " + ex.operand());
        } catch (RecognitionException recognitionException) {
            recognitionException.printStackTrace();
        }
    }

    class MyListener extends ExampleExpressionsQueryBaseListener {
        private Map<String, String> systemSearchableFields;
        private Set<String> foundFieldNames;

        public MyListener(Map<String, String> systemSearchableFields) {
            systemSearchableFields = systemSearchableFields;
            //fieldManager.getSystemSearchableFields().stream().collect(Collectors.toMap(Field::getName, Field::getId));
        }

        public Set<String> getFoundFieldNames() {
            return this.foundFieldNames;
        }

        @Override
        public void enterField(ExampleExpressionsQueryParser.FieldContext ctx) {
            if (ctx.FIELD_NAME() == null)
                return;

            System.out.println("ENTER RULE FIELD NAME = " + ctx.FIELD_NAME());

            *//*if (systemSearchableFields.containsKey(fieldName)) {
                foundFieldNames.add(fieldName);
            }*//*
        }

        @Override
        public void exitField(ExampleExpressionsQueryParser.FieldContext ctx) {
            if (ctx.FIELD_NAME() == null)
                return;
            System.out.println("EXIT RULE FIELD NAME = " + ctx.FIELD_NAME());
           *//* if (systemSearchableFields.containsKey(fieldName)) {
                foundFieldNames.add(fieldName);
            }*//*
        }
    }
*/

/*
    class MyVisitor extends ExampleExpressionsQueryBaseVisitor<String> {

        @Override
        public String visitField(ExampleExpressionsQueryParser.FieldContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitRaw_value(ExampleExpressionsQueryParser.Raw_valueContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitC_op(ExampleExpressionsQueryParser.C_opContext ctx) {
            System.out.println(ctx.start);
            System.out.println(ctx.stop);
            return "C_OP HERE";
        }
    }
}
*/
/*
        enum CompareOperator  {
        GT, LT, GTE, LTE, EQ, NEQ;

        @Nullable
        static CompareOperator fromString(String string) {
            switch (string) {
                case ">":
                    return GT;
                case "<":
                    return LT;
                case ">=":
                    return GTE;
                case "<=":
                    return LTE;
                case "==":
                    return EQ;
                case "!=":
                    return NEQ;
            }
            return null;
        }
    }*/
