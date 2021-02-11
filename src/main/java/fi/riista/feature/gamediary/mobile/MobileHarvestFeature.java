package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.pilot.DeerPilotService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestService;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static fi.riista.feature.gamediary.GameDiarySpecs.harvestsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.shooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.temporalSort;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MobileHarvestFeature {

    @Resource
    private HarvestRepository harvestRepository;

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
    private DeerPilotService deerPilotService;

    @Resource
    private MobileHarvestDTOTransformer dtoTransformer;

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

        // TODO overriding HarvestSpecVersion will be removed when deer pilot 2020 is over.
        final HarvestSpecVersion overrideSpecVersion =
                specVersion.revertIfNotOnDeerPilot(deerPilotService.isPilotUser(person));

        return dtoTransformer.apply(harvests, overrideSpecVersion);
    }

    @Transactional(readOnly = true)
    public MobileHarvestDTO getExistingByMobileClientRefId(final MobileHarvestDTO dto,
                                                           final Person authenticatedPerson) {
        assertHarvestDTOIsValid(dto);

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
        fixNonNullAntlerFieldsIfNotAdultMale(dto);
        assertHarvestDTOIsValid(dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.requirePerson();

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        // TODO `overrideSpecVersion` will be removed when deer pilot 2020 is over.
        final boolean isDeerPilotUser = deerPilotService.isPilotUser(currentPerson);
        final HarvestSpecVersion overrideSpecVersion = specVersion.revertIfNotOnDeerPilot(isDeerPilotUser);

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
                harvestService.updateMutableFields(harvest, dto, activeUser, true, isDeerPilotUser);

        harvestRepository.saveAndFlush(harvest);

        if (dto.getSpecimens() != null) {
            // Do not disable input validation unless really needed.
            harvestSpecimenService
                    .addSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), overrideSpecVersion, false);
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return dtoTransformer.apply(harvest, overrideSpecVersion);
    }

    @Transactional
    public MobileHarvestDTO updateHarvest(final MobileHarvestDTO dto) {
        fixNonNullAntlerFieldsIfNotAdultMale(dto);
        assertHarvestDTOIsValid(dto);

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.requirePerson();

        // TODO `overrideSpecVersion` will be removed when deer pilot 2020 is over.
        final boolean isDeerPilotUser = deerPilotService.isPilotUser(currentPerson);
        final HarvestSpecVersion overrideSpecVersion = specVersion.revertIfNotOnDeerPilot(isDeerPilotUser);

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final boolean businessFieldsCanBeUpdated =
                harvestService.canBusinessFieldsBeUpdatedFromMobile(currentPerson, harvest, overrideSpecVersion);

        final HarvestChangeHistory historyEvent = harvestService
                .updateMutableFields(harvest, dto, activeUser, businessFieldsCanBeUpdated, isDeerPilotUser);

        if (businessFieldsCanBeUpdated) {
            final boolean anyChangesDetected = harvestSpecimenService
                    // Do not disable input validation unless really needed.
                    .setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), overrideSpecVersion, false)
                    .apply((specimens, changesDetected) -> changesDetected);

            if (anyChangesDetected) {
                harvest.forceRevisionUpdate();
            }
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return dtoTransformer.apply(harvestRepository.saveAndFlush(harvest), overrideSpecVersion);
    }

    @Transactional
    public void deleteHarvest(final long harvestId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.DELETE);

        harvestSpecimenService.deleteAllSpecimens(harvest);
        gameDiaryImageService.deleteGameDiaryImages(harvest);
        harvestService.deleteHarvest(harvest, activeUser);
    }

    private static void assertHarvestDTOIsValid(final MobileHarvestDTO dto) {
        if (dto.getGeoLocation().getSource() == null) {
            throw new MessageExposableValidationException("geoLocation.source is null");
        }

        if (dto.getId() == null && dto.getMobileClientRefId() == null) {
            throw new MessageExposableValidationException("mobileClientRefId must not be null");
        }

        // Specimens are allowed to be null on creation.
        if (F.hasId(dto) && dto.getSpecimens() == null) {
            throw new MessageExposableValidationException("specimens must not be null");
        }
    }

    private static void fixNonNullAntlerFieldsIfNotAdultMale(final MobileHarvestDTO dto) {
        Optional.ofNullable(dto.getSpecimens())
                .ifPresent(specimens -> specimens.forEach(specimen -> {
                    if (!specimen.isAdultMale()) {
                        specimen.clearAllAntlerFields();
                    }
                }));
    }
}
