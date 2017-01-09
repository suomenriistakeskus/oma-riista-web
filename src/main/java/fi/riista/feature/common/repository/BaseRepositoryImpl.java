package fi.riista.feature.common.repository;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NoRepositoryBean
public class BaseRepositoryImpl<T, ID extends Serializable>
        extends QueryDslJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityPath<T> path;
    private final Querydsl querydsl;

    public BaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
                              EntityManager entityManager) {
        super(entityInformation, entityManager);
        path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        final PathBuilder<T> builder = new PathBuilder<>(path.getType(), path.getMetadata());
        this.querydsl = new Querydsl(entityManager, builder);
    }

    @Override
    public Slice<T> findAllAsSlice(final Predicate predicate,
                                   final Pageable page) {
        final JPQLQuery<T> query = createQuery(predicate)
                .select(path)
                .offset(page.getOffset())
                .limit(page.getPageSize() + 1);
        return toSlice(querydsl.applySorting(page.getSort(), query).fetch(), page);
    }

    private static <S> Slice<S> toSlice(final List<S> results,
                                        final Pageable page) {
        boolean hasNext = false;
        if (results.size() > page.getPageSize()) {
            // Remove the extra element
            results.remove(results.size() - 1);
            hasNext = true;
        }
        return new SliceImpl<>(results, page, hasNext);
    }

    // findAll returning list

    @Override
    public List<T> findAllAsList(Predicate predicate) {
        return iterableToList(findAll(predicate));
    }

    @Override
    public List<T> findAllAsList(Predicate predicate, Sort sort) {
        return iterableToList(findAll(predicate, sort));
    }

    @Override
    public List<T> findAllAsList(Predicate predicate, OrderSpecifier<?>... orders) {
        return iterableToList(findAll(predicate, orders));
    }

    @Override
    public List<T> findAllAsList(OrderSpecifier<?>... orders) {
        return iterableToList(findAll(orders));
    }

    // findAll returning stream

    @Override
    public Stream<T> findAllAsStream(Predicate predicate) {
        return iterableToStream(findAll(predicate));
    }

    @Override
    public Stream<T> findAllAsStream(Predicate predicate, Sort sort) {
        return iterableToStream(findAll(predicate, sort));
    }

    @Override
    public Stream<T> findAllAsStream(Predicate predicate, OrderSpecifier<?>... orders) {
        return iterableToStream(findAll(predicate, orders));
    }

    @Override
    public Stream<T> findAllAsStream(OrderSpecifier<?>... orders) {
        return iterableToStream(findAll(orders));
    }

    // iterable conversions

    @Override
    public List<T> iterableToList(Iterable<T> i) {
        return ImmutableList.copyOf(i);
    }

    @Override
    public Stream<T> iterableToStream(Iterable<T> i) {
        return StreamSupport.stream(i.spliterator(), false);
    }

}
