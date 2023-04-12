package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.EntityLifecycleFields_;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiarySpecs;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.DeletedHarvest;
import fi.riista.feature.gamediary.harvest.DeletedHarvestRepository;
import fi.riista.feature.gamediary.harvest.DeletedHarvest_;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestService;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaPreds;
import fi.riista.util.jpa.JpaSpecs;
import org.apache.xmlbeans.impl.xpath.XQuery;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static fi.riista.feature.gamediary.GameDiarySpecs.author;
import static fi.riista.feature.gamediary.GameDiarySpecs.harvestsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.observer;
import static fi.riista.feature.gamediary.GameDiarySpecs.shooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.temporalSort;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MobileHarvestFeature {

    private static final int SYNC_PAGE_SIZE = 50;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private DeletedHarvestRepository deletedHarvestRepository;

    @Resource
    private HarvestService harvestService;

    @Resource
    private HarvestSpecimenService harvestSpecimenService;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MobileHarvestDTOTransformer dtoTransformer;

    @Resource
    private MobileHarvestService mobileHarvestService;

    @Transactional(readOnly = true)
    public List<MobileHarvestDTO> getHarvests(final int firstCalendarYearOfHuntingYear,
                                              @Nonnull final HarvestSpecVersion specVersion) {
        requireNonNull(specVersion);

        final Person person = activeUserService.requireActivePerson();

        // Harvest-specific authorization built into query
        final List<Harvest> harvests = harvestRepository.findAll(
                where(shooter(person))
                        .and(harvestsByHuntingYear(firstCalendarYearOfHuntingYear)),
                temporalSort(Direction.ASC));

        return dtoTransformer.apply(harvests, specVersion);
    }

    @Transactional(readOnly = true)
    public MobileDiaryEntryPageDTO<MobileHarvestDTO> fetchPageForActiveUser(final LocalDateTime modifiedAfter,
                                                                                final HarvestSpecVersion specVersion) {
        requireNonNull(specVersion);

        final Person person = activeUserService.requireActivePerson();


        final Specification<Harvest> specs = Specification
                .where(shooter(person))
                .and(modifiedAfter == null
                        ? null
                        : JpaSpecs.modificationTimeAfter(DateUtil.toDateTimeNullSafe(modifiedAfter)));

        final JpaSort sort = JpaSort.of(Sort.Direction.ASC,
                JpaSort.path(SrvaEvent_.lifecycleFields).dot(EntityLifecycleFields_.modificationTime),
                JpaSort.path(SrvaEvent_.id));

        final Page<Harvest> page = harvestRepository.findAll(specs, PageRequest.of(0, SYNC_PAGE_SIZE, sort));
        final List<Harvest> content = page.getContent();

        final DateTime latestModificationTime = content.isEmpty()
                ? null
                : content.get(content.size() - 1).getModificationTime();


        final List<Harvest> otherWithSameModificationTime = page.hasNext()
                ? fetchMissingForTimestamp(F.getUniqueIds(content), person, latestModificationTime)
                : emptyList();

        final List<Harvest> entities = ImmutableList.<Harvest>builder()
                .addAll(content)
                .addAll(otherWithSameModificationTime)
                .build();

        final List<MobileHarvestDTO> dtos =
                dtoTransformer.apply(entities, specVersion);

        return new MobileDiaryEntryPageDTO<>(dtos, DateUtil.toLocalDateTimeNullSafe(latestModificationTime), page.hasNext());
    }

    @Transactional(readOnly = true)
    public MobileHarvestDTO getExistingByMobileClientRefId(final MobileHarvestDTO dto,
                                                           final Person authenticatedPerson) {
        mobileHarvestService.assertHarvestDTOIsValid(dto);

        if (dto.getMobileClientRefId() != null) {
            final Harvest harvest =
                    harvestRepository.findByAuthorAndMobileClientRefId(authenticatedPerson, dto.getMobileClientRefId());

            if (harvest != null) {
                return dtoTransformer.apply(harvest, dto.getHarvestSpecVersion());
            }
        }

        return null;
    }

    @Transactional
    public MobileHarvestDTO createHarvest(final MobileHarvestDTO dto) {
        mobileHarvestService.fixNonNullAntlerFieldsIfNotAdultMale(dto);
        mobileHarvestService.assertHarvestDTOIsValid(dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.requirePerson();

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        // Duplicate prevention check
        final MobileHarvestDTO existing = getExistingByMobileClientRefId(dto, currentPerson);

        if (existing != null) {
            return existing;
        }

        // Not duplicate, create new one

        final Harvest harvest = new Harvest();
        harvest.setAuthor(currentPerson);
        harvest.setActualShooter(currentPerson);
        harvest.setFromMobile(true);
        harvest.setMobileClientRefId(dto.getMobileClientRefId());

        final HarvestChangeHistory historyEvent =
                harvestService.updateMutableFields(harvest, dto, activeUser, true);

        harvestRepository.saveAndFlush(harvest);

        if (dto.getSpecimens() != null) {
            // Do not disable input validation unless really needed.
            harvestSpecimenService
                    .addSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), specVersion, false);
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return dtoTransformer.apply(harvest, specVersion);
    }

    @Transactional
    public MobileHarvestDTO updateHarvest(final MobileHarvestDTO dto) {
        mobileHarvestService.fixNonNullAntlerFieldsIfNotAdultMale(dto);
        mobileHarvestService.assertHarvestDTOIsValid(dto);

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.requirePerson();

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final boolean businessFieldsCanBeUpdated =
                harvestService.canBusinessFieldsBeUpdatedFromMobile(currentPerson, harvest, specVersion, false);

        final HarvestChangeHistory historyEvent = harvestService
                .updateMutableFields(harvest, dto, activeUser, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            final boolean anyChangesDetected = harvestSpecimenService
                    // Do not disable input validation unless really needed.
                    .setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), specVersion, false)
                    .apply((specimens, changesDetected) -> changesDetected);

            if (anyChangesDetected) {
                harvest.forceRevisionUpdate();
            }
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return dtoTransformer.apply(harvestRepository.saveAndFlush(harvest), specVersion);
    }

    @Transactional
    public void deleteHarvest(final long harvestId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.DELETE);

        harvestSpecimenService.deleteAllSpecimens(harvest);
        gameDiaryImageService.deleteGameDiaryImages(harvest);
        harvestService.deleteHarvest(harvest, activeUser);
    }

    @Transactional(readOnly = true)
    public MobileDeletedDiaryEntriesDTO getDeletedHarvestIds(final LocalDateTime modifiedAfter) {
        final List<DeletedHarvest> deletedHarvests = deletedHarvestRepository.findAll(
                deletionTimeNewerThan(DateUtil.toDateTimeNullSafe(modifiedAfter),
                        activeUserService.requireActivePerson().getId()));

        final DateTime latestDeletion = deletedHarvests.stream()
                .map(DeletedHarvest::getDeletionTime)
                .max(DateTimeComparator.getInstance())
                .orElse(null);

        return new MobileDeletedDiaryEntriesDTO(
                DateUtil.toLocalDateTimeNullSafe(latestDeletion), F.mapNonNullsToList(deletedHarvests, DeletedHarvest::getHarvestId));
    }

    @Transactional(readOnly = true)
    public MobileDeletedDiaryEntriesDTO getHarvestsWhereOnlyAuthor() {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person activePerson = activeUser.requirePerson();

        final List<Long> harvestIdsWhereOnlyAuthor = harvestRepository.getHarvestIdsWhereOnlyAuthor(activePerson);
        return new MobileDeletedDiaryEntriesDTO(null, harvestIdsWhereOnlyAuthor);
    }

    private static Specification<DeletedHarvest> deletionTimeNewerThan(
            @Nullable final DateTime deletedSince,
            final long activePersonId) {

        return (root, query, cb) -> {
            final Path<Long> authorId = root.get(DeletedHarvest_.authorId);
            final Path<Long> shooterId = root.get(DeletedHarvest_.shooterId);
            final Predicate authorOrShooter = cb.or(cb.equal(authorId, activePersonId), cb.equal(shooterId, activePersonId));

            if ( deletedSince == null) {
                return authorOrShooter;
            }

            final Path<DateTime> dateField =
                    root.get(DeletedHarvest_.deletionTime);
            return cb.and(cb.greaterThan(dateField, deletedSince),
                    authorOrShooter);
        };
    }

    private List<Harvest> fetchMissingForTimestamp(final Set<Long> alreadyFoundIds,
                                                   final Person person,
                                                   final DateTime latestModificationTime) {
        if (latestModificationTime == null) {
            return emptyList();
        }

        // If more than pageful was found, find rest with same modification time to avoid issues in sync
        final Specification<Harvest> specs = Specification
                .where(shooter(person))
                .and(JpaSpecs.modificationTimeEqual(latestModificationTime))
                .and(not(JpaSpecs.inCollection(Harvest_.id, alreadyFoundIds)));

        return harvestRepository.findAll(specs);
    }
}
