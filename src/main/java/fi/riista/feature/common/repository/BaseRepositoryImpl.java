package fi.riista.feature.common.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;

import fi.riista.util.F;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class BaseRepositoryImpl<T, ID extends Serializable>
        extends QueryDslJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityPath<T> path;
    private final Querydsl querydsl;

    public BaseRepositoryImpl(final JpaEntityInformation<T, ID> entityInformation, final EntityManager entityManager) {
        super(entityInformation, entityManager);
        path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        final PathBuilder<T> builder = new PathBuilder<>(path.getType(), path.getMetadata());
        this.querydsl = new Querydsl(entityManager, builder);
    }

    @Override
    public Slice<T> findAllAsSlice(final Predicate predicate, final Pageable page) {
        final JPQLQuery<T> query = createQuery(predicate)
                .select(path)
                .offset(page.getOffset())
                .limit(page.getPageSize() + 1);
        return toSlice(querydsl.applySorting(page.getSort(), query).fetch(), page);
    }

    private static <S> Slice<S> toSlice(final List<S> results, final Pageable page) {
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
    public List<T> findAllAsList(final Predicate predicate) {
        return findAll(predicate);
    }

    @Override
    public List<T> findAllAsList(final Predicate predicate, final Sort sort) {
        return findAll(predicate, sort);
    }

    @Override
    public List<T> findAllAsList(final Predicate predicate, final OrderSpecifier<?>... orders) {
        return findAll(predicate, orders);
    }

    @Override
    public List<T> findAllAsList(final OrderSpecifier<?>... orders) {
        return findAll(orders);
    }

    // findAll returning stream

    @Override
    public Stream<T> findAllAsStream(final Predicate predicate) {
        return F.stream(findAll(predicate));
    }

    @Override
    public Stream<T> findAllAsStream(final Predicate predicate, final Sort sort) {
        return F.stream(findAll(predicate, sort));
    }

    @Override
    public Stream<T> findAllAsStream(final Predicate predicate, final OrderSpecifier<?>... orders) {
        return F.stream(findAll(predicate, orders));
    }

    @Override
    public Stream<T> findAllAsStream(final OrderSpecifier<?>... orders) {
        return F.stream(findAll(orders));
    }

}
