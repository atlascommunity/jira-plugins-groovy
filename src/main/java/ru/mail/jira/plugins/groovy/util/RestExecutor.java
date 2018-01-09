package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.commons.StreamRestResult;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RestExecutor<T> {
    private static final Logger log = LoggerFactory.getLogger(RestExecutor.class);

    private final Map<Class, Function<Exception, Map<String, Object>>> exceptionMappers = new HashMap<>();
    private final RestExecutorSupplier<T> supplier;

    public RestExecutor(RestExecutorSupplier<T> supplier) {
        this.supplier = supplier;
    }

    public <ET extends Exception> RestExecutor<T> withExceptionMapper(Class<ET> clazz, Function<ET, Map<String, Object>> mapper) {
        exceptionMappers.put(clazz, (Function<Exception, Map<String, Object>>) mapper);
        return this;
    }

    public Response getResponse() {
        return getResponse(Response.Status.OK);
    }

    public Response getResponse(Response.Status successStatus) {
        try {
            T actionResult = supplier.get();
            Response.ResponseBuilder responseBuilder = Response.status(successStatus).entity(actionResult);

            if (actionResult instanceof byte[])
                responseBuilder = responseBuilder.type("application/force-download")
                        .header("Content-Transfer-Encoding", "binary")
                        .header("charset", "UTF-8");
            else if(actionResult instanceof StreamRestResult)
                responseBuilder = responseBuilder.entity(((StreamRestResult) actionResult).getInputStream())
                        .type(((StreamRestResult) actionResult).getContentType());
            return responseBuilder.build();
        } catch (SecurityException e) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(ImmutableMap.of("message", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (IllegalArgumentException e) {
            Map<String, String> entity = new HashMap<>();
            entity.put("message", e.getMessage());

            Response.ResponseBuilder responseBuilder = Response
                .status(Response.Status.BAD_REQUEST)
                .entity(entity);
            if (e instanceof RestFieldException) {
                entity.put("field", ((RestFieldException) e).getField());
            }
            return responseBuilder.build();
        } catch (Exception e) {
            Map<String, Object> entity = null;

            for (Map.Entry<Class, Function<Exception, Map<String, Object>>> entry : exceptionMappers.entrySet()) {
                Class type = entry.getKey();

                if (type.isInstance(e)) {
                    entity = entry.getValue().apply(e);
                }
            }

            if (entity == null) {
                entity = ImmutableMap.of("message", e.getMessage());
            }

            log.error("REST Exception", e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(entity)
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }
}
