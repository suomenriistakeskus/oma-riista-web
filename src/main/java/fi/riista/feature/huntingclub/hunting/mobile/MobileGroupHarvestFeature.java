package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestService;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.mobile.MobileHarvestService;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DtoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.harvest.HarvestAuthorization.Permission.LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP;

@Service
public class MobileGroupHarvestFeature {

    @Resource
    private MobileHarvestService mobileHarvestService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private MobileGroupHarvestDTOTransformer dtoTransformer;

    @Resource
    private HarvestService harvestService;

    @Resource
    private HarvestSpecimenService harvestSpecimenService;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional
    public MobileGroupHarvestDTO createHarvest(final MobileGroupHarvestDTO dto) {
        mobileHarvestService.fixNonNullAntlerFieldsIfNotAdultMale(dto);
        mobileHarvestService.assertHarvestDTOIsValid(dto);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.requirePerson();

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        // Duplicate prevention check
        final MobileGroupHarvestDTO existing = getExistingByMobileClientRefId(dto, currentPerson);

        if (existing != null) {
            return existing;
        }

        // Not duplicate, create new one

        final Harvest harvest = new Harvest();
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
    public MobileGroupHarvestDTO updateHarvest(final MobileGroupHarvestDTO dto) {
        mobileHarvestService.fixNonNullAntlerFieldsIfNotAdultMale(dto);
        mobileHarvestService.assertHarvestDTOIsValid(dto);

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = activeUser.requirePerson();

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final boolean businessFieldsCanBeUpdated =
                harvestService.canBusinessFieldsBeUpdatedFromMobile(currentPerson, harvest, specVersion, true);

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

    private MobileGroupHarvestDTO getExistingByMobileClientRefId(final MobileGroupHarvestDTO dto,
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
}
