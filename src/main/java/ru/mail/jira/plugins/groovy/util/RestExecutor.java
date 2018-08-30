package ru.mail.jira.plugins.groovy.util;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RestExecutor<T> {
    private static final Logger log = LoggerFactory.getLogger(RestExecutor.class);

    private final Map<Class, ExceptionMapper> exceptionMappers = new HashMap<>();
    private final RestExecutorSupplier<T> supplier;

    public RestExecutor(RestExecutorSupplier<T> supplier) {
        this.supplier = supplier;
    }

    public <ET extends Exception> RestExecutor<T> withExceptionMapper(Class<ET> clazz, Response.Status status, Function<ET, Map<String, Object>> mapper) {
        exceptionMappers.put(clazz, new ExceptionMapper(status, (Function<Exception, Map<String, Object>>) mapper));
        return this;
    }

    public Response getResponse() {
        return getResponse(Response.Status.OK);
    }

    public Response getResponse(Response.Status successStatus) {
        try {
            T actionResult = supplier.get();

            if (actionResult instanceof Response) {
                return (Response) actionResult;
            }

            Response.ResponseBuilder responseBuilder = Response.status(successStatus).entity(actionResult);

            if (actionResult instanceof byte[])
                responseBuilder = responseBuilder.type("application/force-download")
                    .header("Content-Transfer-Encoding", "binary")
                    .header("charset", "UTF-8");
            return responseBuilder.build();
        } catch (SecurityException e) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(ImmutableMap.of("message", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ValidationException e) {
            Map<String, Object> entity = new HashMap<>();
            entity.put("messages", e.getMessages());
            entity.put("field", e.getField());

            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(entity)
                .build();
        } catch (IllegalArgumentException e) {
            Map<String, String> entity = new HashMap<>();
            entity.put("message", e.getMessage());

            if (e instanceof RestFieldException) {
                entity.put("field", ((RestFieldException) e).getField());
            }
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(entity)
                .build();
        } catch (Exception e) {
            Map<String, Object> entity = null;

            Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
            boolean handled = false;

            for (Map.Entry<Class, ExceptionMapper> entry : exceptionMappers.entrySet()) {
                Class type = entry.getKey();

                if (type.isInstance(e)) {
                    ExceptionMapper mapper = entry.getValue();
                    entity = mapper.getFunction().apply(e);
                    status = mapper.getStatus();
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                entity = ImmutableMap.of(
                    "message", e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName(),
                    "stack-trace", ExceptionHelper.writeExceptionToString(e)
                );
                log.error("REST Exception", e);
            } else {
                log.trace("Handled exception", e);
            }

            return Response
                .status(status)
                .entity(entity)
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ExceptionMapper {
        private final Response.Status status;
        private final Function<Exception, Map<String, Object>> function;
    }
}
