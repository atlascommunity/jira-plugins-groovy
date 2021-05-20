package ru.mail.jira.plugins.groovy.impl.jql.function.builtin.expression;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.syntax.Types;
import ru.mail.jira.plugins.groovy.util.cl.ClassLoaderUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShellUtils {

    public static GroovyClassLoader createSecureClassLoader(CompilationCustomizer... additionalCompilationCustomizers) {
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer();
        secureASTCustomizer.setClosuresAllowed(false);
        secureASTCustomizer.setMethodDefinitionAllowed(false);
        secureASTCustomizer.setIndirectImportCheckEnabled(false);
        secureASTCustomizer.setImportsWhitelist(Collections.emptyList());
        secureASTCustomizer.setStaticImportsWhitelist(Collections.emptyList());

        List<Class<? extends Expression>> expressionsWhiteList = Stream.of(BinaryExpression.class,
                                                                           VariableExpression.class,
                                                                           ConstantExpression.class,
                                                                           MethodCallExpression.class,
                                                                           ArgumentListExpression.class,
                                                                           PropertyExpression.class,
                                                                           CastExpression.class)
                                                                       .collect(Collectors.toList());
        List<Integer> tokensWhiteList = Stream.of(Types.PLUS,
                                                  Types.MINUS,
                                                  Types.MULTIPLY,
                                                  Types.DIVIDE,
                                                  Types.COMPARE_EQUAL,
                                                  Types.COMPARE_NOT_EQUAL,
                                                  Types.COMPARE_LESS_THAN,
                                                  Types.COMPARE_LESS_THAN_EQUAL,
                                                  Types.COMPARE_GREATER_THAN,
                                                  Types.COMPARE_GREATER_THAN_EQUAL,
                                                  Types.LEFT_SQUARE_BRACKET,
                                                  Types.RIGHT_SQUARE_BRACKET)
                                              .collect(Collectors.toList());

        List<Class> constantTypesWhiteList = Stream.of(Object.class,
                                                       Integer.class,
                                                       Float.class,
                                                       Long.class,
                                                       Double.class,
                                                       BigDecimal.class,
                                                       Date.class,
                                                       Integer.TYPE,
                                                       Long.TYPE,
                                                       Float.TYPE,
                                                       Double.TYPE,
                                                       String.class,
                                                       Timestamp.class)
                                                   .collect(Collectors.toList());

        List<Class> receiversWhiteList = Stream.of(Object.class,
                                                   Math.class,
                                                   Integer.class,
                                                   Float.class,
                                                   Long.class,
                                                   Double.class,
                                                   BigDecimal.class,
                                                   Date.class,
                                                   BigDecimal.class,
                                                   Timestamp.class)
                                               .collect(Collectors.toList());

        secureASTCustomizer.setExpressionsWhitelist(expressionsWhiteList);
        secureASTCustomizer.setTokensWhitelist(tokensWhiteList);
        secureASTCustomizer.setConstantTypesClassesWhiteList(constantTypesWhiteList);
        secureASTCustomizer.setReceiversClassesWhiteList(receiversWhiteList);
        secureASTCustomizer.setStaticStarImportsWhitelist(Collections.singletonList(Math.class.getName()));

        ImportCustomizer importCustomizer = new ImportCustomizer().addStaticStars(Math.class.getName());


        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(secureASTCustomizer, importCustomizer);
        compilerConfiguration.addCompilationCustomizers(additionalCompilationCustomizers);

        return new GroovyClassLoader(ClassLoaderUtil.getCurrentPluginClassLoader(), compilerConfiguration);
    }
}
