package fi.riista.util;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.error.RevisionConflictException;
import io.vavr.Tuple2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class DtoUtil {

    private DtoUtil() {
        throw new AssertionError();
    }

    // Page of entities
    public static <ID extends Serializable, E extends BaseEntity<ID>, D extends BaseEntityDTO<ID>> Page<D> toDTO(
            @Nonnull final Page<E> resultPage,
            @Nonnull final Pageable pageRequest,
            @Nonnull final Function<E, D> function) {

        Objects.requireNonNull(resultPage, "resultPage must not be null");
        Objects.requireNonNull(pageRequest, "pageRequest must not be null");
        Objects.requireNonNull(function, "function must not be null");

        final List<D> list = resultPage.getContent().stream().map(function).collect(toList());

        return new PageImpl<>(list, pageRequest, resultPage.getTotalElements());
    }

    public static <ID extends Serializable> void assertNoVersionConflict(@Nonnull final BaseEntity<ID> entity,
                                                                         @Nonnull final BaseEntityDTO<ID> dto) {

        if (checkForVersionConflict(entity, dto)) {
            final String msg =
                String.format("Update is in conflict with current resource version. %s:%s:%s, %s:%s:%s",
                        entity.getClass().getSimpleName(),
                        entity.getId(),
                        entity.getConsistencyVersion(),
                        dto.getClass().getSimpleName(),
                        dto.getId(),
                        dto.getRev());
            throw new RevisionConflictException(msg);
        }
    }

    public static <ID extends Serializable> boolean checkForVersionConflict(@Nonnull final BaseEntity<ID> entity,
                                                                            @Nonnull final BaseEntityDTO<ID> dto) {

        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(dto, "dto must not be null");

        return checkForVersionConflict(entity, dto.getRev());
    }

    public static <ID extends Serializable> void assertNoVersionConflict(@Nonnull final BaseEntity<ID> entity,
                                                                         @Nullable final Integer rev) {
        if (checkForVersionConflict(entity, rev)) {
            final String msg =
                    String.format("Update is in conflict with current resource version. %s:%s:%s, ::%s",
                            entity.getClass().getSimpleName(),
                            entity.getId(),
                            entity.getConsistencyVersion(),
                            rev);
            throw new RevisionConflictException(msg);
        }
    }

    public static <ID extends Serializable> boolean checkForVersionConflict(@Nonnull final BaseEntity<ID> entity,
                                                                            @Nullable final Integer rev) {

        Objects.requireNonNull(entity, "entity must not be null");

        if (rev == null || entity.getConsistencyVersion() == null) {
            // Cannot check without version information
            return false;
        }

        // Persisted entity should have earlier
        return entity.getConsistencyVersion() > rev;
    }

    public static <ID extends Serializable> void copyBaseFields(@Nonnull final BaseEntity<ID> entity,
                                                                @Nonnull final BaseEntityDTO<ID> dto) {

        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(dto, "dto must not be null");

        dto.setId(entity.getId());
        dto.setRev(entity.getConsistencyVersion());
    }

    public static <ID extends Serializable> boolean equalIdsAndVersions(
            @Nonnull final Iterable<? extends BaseEntity<ID>> entities,
            @Nonnull final Iterable<? extends BaseEntityDTO<ID>> dtos) {

        Objects.requireNonNull(entities, "entities must not be null");
        Objects.requireNonNull(dtos, "dtos must not be null");

        final Set<Tuple2<ID, Integer>> entityIdsAndVersions =
                F.stream(entities).map(Functions.idAndVersion()).collect(toSet());

        final Set<Tuple2<ID, Integer>> dtoIdsAndVersions =
                F.stream(dtos).map(Functions.dtoIdAndVersion()).collect(toSet());

        return entityIdsAndVersions.equals(dtoIdsAndVersions);
    }
}
