package fi.riista.util;

import com.google.common.collect.Iterables;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

public class JdbcTemplateUtils {
    public static Double queryForDouble(final String sql,
                                        final Double defaultValue,
                                        final NamedParameterJdbcOperations template,
                                        final SqlParameterSource parameterSource) {
        return queryForSingleValue(sql, defaultValue, template, parameterSource, Double.class);
    }

    public static <T> T queryForSingleValue(final String sql,
                                            final T defaultValue,
                                            final NamedParameterJdbcOperations template,
                                            final SqlParameterSource paramSource,
                                            final Class<T> requiredType) {
        final List<T> listResult = template.queryForList(sql, paramSource, requiredType);
        return Iterables.getFirst(listResult, defaultValue);
    }

    private JdbcTemplateUtils() {
        throw new AssertionError();
    }
}
