package fi.riista.feature.common.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends
        JpaRepository<T, ID>,
        JpaSpecificationExecutor<T>,
        QuerydslPredicateExecutor<T> {

    Slice<T> findAllAsSlice(Predicate predicate, Pageable page);

    Slice<T> findAllAsSlice(Predicate predicate, Pageable page, OrderSpecifier<?>... orders);
    // findAll returning list

    List<T> findAllAsList(Predicate predicate);

    List<T> findAllAsList(Predicate predicate, Sort sort);

    List<T> findAllAsList(Predicate predicate, OrderSpecifier<?>... orders);

    List<T> findAllAsList(OrderSpecifier<?>... orders);

    // findAll returning stream

    Stream<T> findAllAsStream(Predicate predicate);

    Stream<T> findAllAsStream(Predicate predicate, Sort sort);

    Stream<T> findAllAsStream(Predicate predicate, OrderSpecifier<?>... orders);

    Stream<T> findAllAsStream(OrderSpecifier<?>... orders);

}
