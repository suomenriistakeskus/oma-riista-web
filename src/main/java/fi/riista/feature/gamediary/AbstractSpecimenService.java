package fi.riista.feature.gamediary;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static fi.riista.util.Filters.hasAnyIdOf;
import static fi.riista.util.Filters.idNotAnyOf;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.stream.Collectors.toList;

public abstract class AbstractSpecimenService<PARENT extends BaseEntity<Long>, ENTITY extends BaseEntity<Long>, DTO extends BaseEntityDTO<Long>, VERSION extends GameDiaryEntitySpecVersion> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpecimenService.class);

    protected final SingularAttribute<? super ENTITY, PARENT> parentAttribute;
    protected final String parentClassName;

    protected AbstractSpecimenService(@Nonnull final SingularAttribute<? super ENTITY, PARENT> parentAttribute) {
        this.parentAttribute = Objects.requireNonNull(parentAttribute);
        this.parentClassName = parentAttribute.getJavaType().getSimpleName();
    }

    protected abstract <REPO extends BaseRepository<ENTITY, Long>> REPO getSpecimenRepository();

    protected abstract ENTITY createSpecimen(@Nonnull PARENT parent);

    protected abstract boolean hasContent(DTO specimenData);

    protected abstract BiConsumer<DTO, ENTITY> getSpecimenFieldCopier(@Nonnull PARENT parent, @Nonnull VERSION version);

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<ENTITY> addSpecimens(
            @Nonnull final PARENT parent,
            final int totalAmount,
            @Nonnull final List<DTO> dtos,
            @Nonnull final VERSION version) {

        checkParameters(parent, totalAmount, dtos, version);

        final List<ENTITY> ret = createNewSpecimens(parent, filterSpecimensWithContent(dtos), version);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}(id={}): Added {} new specimen(s), set total amount to {}",
                    parentClassName, parent.getId(), ret.size(), totalAmount);
        }

        return ret;
    }

    /**
     *
     * @param parent
     *            - Parent object with which the specimens are associated
     * @param totalAmount
     *            - Total amount of specimens defined for parent
     * @param dtos
     *            - DTO containing data for specimens to be persisted/merged
     * @param version
     * @return pair consisting of (1) specimens existing after method call and (2) boolean value
     *         indicating whether any changes were persisted into database.
     */
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Tuple2<List<ENTITY>, Boolean> setSpecimens(
            @Nonnull final PARENT parent,
            final int totalAmount,
            @Nonnull final List<DTO> dtos,
            @Nonnull final VERSION version) {

        checkParameters(parent, totalAmount, dtos, version);

        final List<ENTITY> existingSpecimens = findExistingSpecimensInInsertionOrder(parent);

        final Map<Boolean, List<DTO>> dtoPartitionByIdExistence =
                F.partition(filterSpecimensWithContent(dtos), F::hasId);
        final List<DTO> updateDtos = dtoPartitionByIdExistence.get(true);

        // Check referential integrity
        if (updateDtos.stream().anyMatch(idNotAnyOf(existingSpecimens))) {
            throw new IllegalArgumentException(String.format(
                    "Some specimen IDs not related to given %s object (common parent)", parentClassName));
        }

        final Map<Boolean, List<ENTITY>> updatedOrRemoved = F.partition(existingSpecimens, hasAnyIdOf(updateDtos));
        final List<ENTITY> entitiesToBeUpdated = updatedOrRemoved.get(true);
        final List<ENTITY> entitiesToBeRemoved = updatedOrRemoved.get(false);

        // Delete existing specimens (the IDs of which are not found in DTOs
        // or "empty" DTOs).
        getSpecimenRepository().deleteInBatch(entitiesToBeRemoved);

        final Map<Long, DTO> updateDtoById = F.indexById(updateDtos);

        // Persist new specimen entities.
        final List<ENTITY> resultSpecimens = createNewSpecimens(parent, dtoPartitionByIdExistence.get(false), version);
        final int numNewSpecimens = resultSpecimens.size();

        // Update existing specimens.
        entitiesToBeUpdated.forEach(persistentEntity -> {
            final DTO dto = updateDtoById.get(persistentEntity.getId());
            DtoUtil.assertNoVersionConflict(persistentEntity, dto);
            getSpecimenFieldCopier(parent, version).accept(dto, persistentEntity);
        });
        resultSpecimens.addAll(0, entitiesToBeUpdated);

        // To have specimen revisions updated for DTO transformation.
        if (!entitiesToBeUpdated.isEmpty()) {
            getSpecimenRepository().flush();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}(id={}) specimen update: added {}, updated/preserved {}, removed {}, total amount set to {}",
                    parentClassName,
                    parent.getId(),
                    numNewSpecimens,
                    entitiesToBeUpdated.size(),
                    entitiesToBeRemoved.size(),
                    totalAmount);
        }

        // True if there exists:
        // (1) new entities to be saved,
        // (2) entities to be removed, or
        // (3) entities whose fields were updated.
        final boolean changesDetected = numNewSpecimens > 0 ||
                !entitiesToBeRemoved.isEmpty() ||
                !DtoUtil.equalIdsAndVersions(entitiesToBeUpdated, updateDtos);

        return Tuple.of(resultSpecimens, changesDetected);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteAllSpecimens(@Nonnull final PARENT parent) {
        requirePersistedEntity(parent);

        final List<ENTITY> allSpecimens = findExistingSpecimens(parent);

        if (!allSpecimens.isEmpty()) {
            getSpecimenRepository().deleteInBatch(allSpecimens);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}(id={}): Removed all ({}) specimen(s)",
                    parentClassName, parent.getId(), allSpecimens.size());
        }
    }

    protected List<ENTITY> findExistingSpecimens(final PARENT parent) {
        return getSpecimenRepository().findAll(equal(parentAttribute, parent));
    }

    protected List<ENTITY> findExistingSpecimensInInsertionOrder(final PARENT parent) {
        return F.sortedById(findExistingSpecimens(parent));
    }

    protected void checkParameters(
            @Nonnull final PARENT parent,
            final int totalAmount,
            @Nonnull final List<DTO> dtos,
            @Nonnull final VERSION version) {

        requirePersistedEntity(parent);
        Objects.requireNonNull(dtos, "dtos is null");
        Objects.requireNonNull(version, "version is null");

        Preconditions.checkArgument(dtos.size() <= totalAmount,
                "Total specimen amount must not be less than number of items in specimen collection");
    }

    protected PARENT requirePersistedEntity(final PARENT parent) {
        Objects.requireNonNull(parent);

        if (parent.isNew()) {
            throw new IllegalArgumentException(
                    String.format("%s object must be persisted prior to being added specimens", parentClassName));
        }

        return parent;
    }

    private List<DTO> filterSpecimensWithContent(final Iterable<DTO> dtos) {
        return F.stream(dtos).filter(Objects::nonNull).filter(this::hasContent).collect(toList());
    }

    private List<ENTITY> createNewSpecimens(final PARENT parent, final List<DTO> dtos, final VERSION version) {
        return dtos.stream()
                .map(dto -> {
                    if (F.hasId(dto)) {
                        throw new IllegalStateException("DTO already has ID");
                    }

                    final ENTITY newSpecimen = createSpecimen(parent);
                    getSpecimenFieldCopier(parent, version).accept(dto, newSpecimen);
                    return getSpecimenRepository().save(newSpecimen);
                })
                .collect(toList());
    }

}
