package fi.riista.feature.gamediary.mobile.srva;

import com.google.common.collect.ImmutableList;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.EntityLifecycleFields_;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.error.RevisionConflictException;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.mobile.MobileDeletedDiaryEntriesDTO;
import fi.riista.feature.gamediary.mobile.MobileDiaryEntryPageDTO;
import fi.riista.feature.gamediary.observation.DeletedObservation;
import fi.riista.feature.gamediary.observation.DeletedObservation_;
import fi.riista.feature.gamediary.srva.AbstractSrvaCrudFeature;
import fi.riista.feature.gamediary.srva.DeletedSrvaEvent;
import fi.riista.feature.gamediary.srva.DeletedSrvaEventRepository;
import fi.riista.feature.gamediary.srva.DeletedSrvaEvent_;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.criteria.Path;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.springframework.data.jpa.domain.Specification.not;

@Service
public class MobileSrvaCrudFeature extends AbstractSrvaCrudFeature<MobileSrvaEventDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(MobileSrvaCrudFeature.class);
    private static final int SYNC_PAGE_SIZE = 50;

    @Resource
    private MobileSrvaEventDTOTransformer mobileSrvaEventDTOTransformer;

    @Resource
    private DeletedSrvaEventRepository deletedSrvaEventRepository;

    @Override
    protected void updateEntity(SrvaEvent entity, MobileSrvaEventDTO dto) {
        updateEntityCommonFields(entity, dto);

        if (dto.getSrvaEventSpecVersion().greaterThanOrEqualTo(SrvaEventSpecVersion._2)) {
            updateEntitySpecV2Fields(entity, dto);
        }

        entity.setMobileClientRefId(dto.getMobileClientRefId());
        entity.setFromMobile(true);
    }

    @Override
    protected MobileSrvaEventDTO toDTO(@Nonnull final SrvaEvent entity) {
        throw new UnsupportedOperationException("No transformation without srvaEventSpecVersion supported");
    }

    @Transactional(readOnly = true)
    public List<MobileSrvaEventDTO> listSrvaEventsForActiveUser(@Nonnull final SrvaEventSpecVersion srvaEventSpecVersion) {
        final Person person = activeUserService.requireActivePerson();
        final Specification<SrvaEvent> specs = Specification.where(SrvaSpecs.author(person));
        final JpaSort sort = JpaSort.of(Sort.Direction.DESC, SrvaEvent_.pointOfTime, SrvaEvent_.id);
        final List<SrvaEvent> events = getRepository().findAll(specs, sort);

        return mobileSrvaEventDTOTransformer.apply(events, srvaEventSpecVersion);
    }

    @Transactional(readOnly = true)
    public MobileDiaryEntryPageDTO<MobileSrvaEventDTO> fetchPageForActiveUser(
            @Nullable final LocalDateTime modifiedAfter,
            @Nonnull final SrvaEventSpecVersion srvaEventSpecVersion) {

        final Person person = activeUserService.requireActivePerson();


        final Specification<SrvaEvent> specs = Specification
                .where(SrvaSpecs.author(person))
                .and(modifiedAfter == null
                        ? null
                        : JpaSpecs.modificationTimeAfter(DateUtil.toDateTimeNullSafe(modifiedAfter)));

        final JpaSort sort = JpaSort.of(Sort.Direction.ASC,
                JpaSort.path(SrvaEvent_.lifecycleFields).dot(EntityLifecycleFields_.modificationTime),
                JpaSort.path(SrvaEvent_.id));

        final Page<SrvaEvent> page = getRepository().findAll(specs, PageRequest.of(0, SYNC_PAGE_SIZE, sort));
        final List<SrvaEvent> content = page.getContent();

        final DateTime latestModificationTime = content.isEmpty()
                ? null
                : content.get(content.size() - 1).getModificationTime();


        final List<SrvaEvent> otherWithSameModificationTime = page.hasNext()
                ? fetchMissingForTimestamp(F.getUniqueIds(content), person, latestModificationTime)
                : emptyList();

        final List<SrvaEvent> entities = ImmutableList.<SrvaEvent>builder()
                .addAll(content)
                .addAll(otherWithSameModificationTime)
                .build();

        final List<MobileSrvaEventDTO> dtos =
                mobileSrvaEventDTOTransformer.apply(entities, srvaEventSpecVersion);

        return new MobileDiaryEntryPageDTO<>(dtos, DateUtil.toLocalDateTimeNullSafe(latestModificationTime), page.hasNext());
    }

    private List<SrvaEvent> fetchMissingForTimestamp(final Set<Long> alreadyFoundIds,
                                                     final Person person,
                                                     final DateTime latestModificationTime) {
        if (latestModificationTime == null) {
            return emptyList();
        }

        // If more than pageful was found, find rest with same modification time to avoid issues in sync
        final Specification<SrvaEvent> specs = Specification
                .where(SrvaSpecs.author(person))
                .and(JpaSpecs.modificationTimeEqual(latestModificationTime))
                .and(not(JpaSpecs.inCollection(SrvaEvent_.id, alreadyFoundIds)));

        return getRepository().findAll(specs);
    }

    @Transactional
    @Override
    public MobileSrvaEventDTO createSrvaEvent(@Nonnull final MobileSrvaEventDTO dto) {
        final Person authenticatedPerson = activeUserService.requireActivePerson();

        assertValidSpecVersion(dto);
        assertValidRefId(dto);

        //Duplicate check
        final SrvaEvent existing = getRepository().findByAuthorAndMobileClientRefId(authenticatedPerson, dto.getMobileClientRefId());

        // Return existing if found, otherwise create new srva event
        return existing == null ?
                mobileSrvaEventDTOTransformer.apply(createSrvaEvent(dto, false), dto.getSrvaEventSpecVersion()) :
                mobileSrvaEventDTOTransformer.apply(existing, dto.getSrvaEventSpecVersion());
    }

    @Transactional
    @Override
    public MobileSrvaEventDTO updateSrvaEvent(@Nonnull final MobileSrvaEventDTO dto) {
        assertValidSpecVersion(dto);

        SrvaEvent updatedEvent;
        try {
            updatedEvent = getRepository().saveAndFlush(updateSrvaEvent(dto, false));
        } catch (NotFoundException | RevisionConflictException e) {
            LOG.info("NotFoundException|RevisionConflictException updating srva event id" + dto.getId() + " " + e.getMessage());
            // if NotFoundException or RevisionConflictException occurs we can throw it and mobile handles it.
            throw e;
        } catch (RuntimeException re) {
            LOG.info("RuntimeException updating srva event id" + dto.getId() + " " + re.getMessage());
            // if any other error happens mobile cannot do anything to it so we return current server version.
            updatedEvent = requireEntity(dto.getId(), EntityPermission.READ);
        }

        return mobileSrvaEventDTOTransformer.apply(updatedEvent, dto.getSrvaEventSpecVersion());
    }

    @Transactional(rollbackFor = IOException.class)
    public void addImage(@Nonnull final Long eventId, @Nonnull final UUID uuid, @Nonnull final MultipartFile file) throws IOException {
        activeUserService.requireActivePerson();

        gameDiaryImageService.addGameDiaryImageWithoutDiaryEntryAssociation(uuid, file);

        final SrvaEvent srvaEvent = getRepository().findById(eventId).get();

        gameDiaryImageService.associateSrvaEventWithImage(srvaEvent, uuid);

    }

    @Transactional
    public void deleteImage(@Nonnull final UUID imageUuid) {
        try {
            final GameDiaryImage image = gameDiaryImageService.getSrvaEventImageForAuthor(imageUuid, activeUserService.requireActivePerson());
            gameDiaryImageService.deleteGameDiaryImage(image);
        } catch (NotFoundException nfe) {
            LOG.info("deleteGameDiaryImage failed, image not found uuid:" + imageUuid);
            // If image is not found there is nothing that mobile client can do so let's not report this
        }
    }

    private static void assertValidSpecVersion(final MobileSrvaEventDTO dto) {
        Objects.requireNonNull(dto);
        if (dto.getSrvaEventSpecVersion() == null) {
            throw new MessageExposableValidationException("srvaEventSpecVersion must not be null");
        }
    }

    private static void assertValidRefId(final MobileSrvaEventDTO dto) {
        Objects.requireNonNull(dto);
        if (dto.getMobileClientRefId() == null) {
            throw new MessageExposableValidationException("mobileClientRefId must not be null");
        }
    }

    @Transactional(readOnly = true)
    public MobileDeletedDiaryEntriesDTO getDeletedEvents(final LocalDateTime deletedAfter) {
        final List<DeletedSrvaEvent> deletedSrvaEvents = deletedSrvaEventRepository.findAll(
                deletionTimeNewerThan(DateUtil.toDateTimeNullSafe(deletedAfter), activeUserService.requireActivePerson().getId()));

        final LocalDateTime latestDeletion = deletedSrvaEvents.stream()
                .map(DeletedSrvaEvent::getDeletionTime)
                .max(DateTimeComparator.getInstance())
                .map(DateUtil::toLocalDateTimeNullSafe)
                .orElse(null);

        return new MobileDeletedDiaryEntriesDTO(
                latestDeletion, F.mapNonNullsToList(deletedSrvaEvents, DeletedSrvaEvent::getSrvaEventId));
    }


    private static Specification<DeletedSrvaEvent> deletionTimeNewerThan(
            @Nullable final DateTime deletedSince,
            final long activePersonId) {

        if (deletedSince == null) {
            return (root, query, cb) -> {
                final Path<Long> authorId = root.get(DeletedSrvaEvent_.authorId);
                return cb.equal(authorId, activePersonId);
            };
        }

        return (root, query, cb) -> {
            final Path<DateTime> dateField =
                    root.get(DeletedSrvaEvent_.deletionTime);
            final Path<Long> authorId = root.get(DeletedSrvaEvent_.authorId);
            return cb.and(cb.greaterThan(dateField, deletedSince), cb.equal(authorId, activePersonId));
        };
    }

}
