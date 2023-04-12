package fi.riista.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import org.springframework.data.domain.Sort;

public class QueryDslUtil {
    public static OrderSpecifier<?>[] getSortedColumns(final Sort sorts, final Path<?> parent) {
        return sorts.stream().map(sort -> {
            final Order order = sort.getDirection().isAscending() ? Order.ASC : Order.DESC;
            final SimplePath<Object> filedPath = Expressions.path(Object.class, parent, sort.getProperty());
            return new OrderSpecifier(order, filedPath);
        }).toArray(OrderSpecifier[]::new);
    }

    private QueryDslUtil() {
        throw new AssertionError();
    }
}
