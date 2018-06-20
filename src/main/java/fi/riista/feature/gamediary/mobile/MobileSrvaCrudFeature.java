package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.error.RevisionConflictException;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.srva.AbstractSrvaCrudFeature;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MobileSrvaCrudFeature extends AbstractSrvaCrudFeature<MobileSrvaEventDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(MobileSrvaCrudFeature.class);

    @Resource
    private MobileSrvaEventDTOTransformer mobileSrvaEventDTOTransformer;

    @Override
    protected void updateEntity(SrvaEvent entity, MobileSrvaEventDTO dto) {
        updateEntityCommonFields(entity, dto);

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
        final Specifications<SrvaEvent> specs = Specifications.where(SrvaSpecs.author(person));
        final JpaSort sort = new JpaSort(Sort.Direction.DESC, SrvaEvent_.pointOfTime, SrvaEvent_.id);
        final List<SrvaEvent> events = getRepository().findAll(specs, sort);

        return mobileSrvaEventDTOTransformer.apply(events, srvaEventSpecVersion);
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

        final SrvaEvent srvaEvent = getRepository().findOne(eventId);

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

}
