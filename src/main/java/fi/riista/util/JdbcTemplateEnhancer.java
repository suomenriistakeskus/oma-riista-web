package fi.riista.util;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class JdbcTemplateEnhancer {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplateEnhancer.class);

    public static NamedParameterJdbcOperations wrap(final NamedParameterJdbcOperations jdbcOperations) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(NamedParameterJdbcOperations.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            final Stopwatch sw = Stopwatch.createStarted();

            try {
                final String methodName = method.getName();

                if (methodName.startsWith("query")
                        || methodName.startsWith("batchUpdate")
                        || methodName.startsWith("update")) {
                    logSqlQuery(args);

                    final Object result = method.invoke(jdbcOperations, args);

                    if (sw.elapsed(TimeUnit.SECONDS) > 5) {
                        LOG.warn("SQL execution took too long {}", sw);
                    }

                    return result;
                } else {
                    return method.invoke(jdbcOperations, args);
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        });

        return (NamedParameterJdbcOperations) enhancer.create();
    }

    private static void logSqlQuery(final Object[] args) {
        final String query = (String) args[0];

        if (args[1] instanceof MapSqlParameterSource) {
            MapSqlParameterSource params = (MapSqlParameterSource) args[1];
            LOG.info("SQL: {} with parameters {}", query, Joiner.on(", ")
                    .withKeyValueSeparator(" = ")
                    .join(params.getValues()));
        } else {
            LOG.info("SQL: {}", query);
        }
    }

    private JdbcTemplateEnhancer() {
        throw new AssertionError();
    }
}
