package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocationRepository;
import fi.riista.feature.harvestpermit.list.MooselikePermitListDTO;
import fi.riista.feature.harvestpermit.payment.MooselikePermitPriceService;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaService;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.feature.huntingclub.permit.statistics.HuntingDayStatisticsService;
import fi.riista.feature.huntingclub.permit.todo.MoosePermitTodoService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.CREATE_REMOVE_MOOSE_HARVEST_REPORT;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.LIST_LEADERS;

@Component
public class HuntingClubPermitDTOFactory {

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private HarvestPermitAllocationRepository allocationRepository;

    @Resource
    private MooselikePermitPriceService mooselikePermitPriceService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestCountService harvestCountService;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    @Resource
    private HuntingDayStatisticsService huntingDayStatisticsService;

    @Resource
    private MoosePermitTodoService moosePermitTodoService;

    @Resource
    private ClubHuntingSummaryBasicInfoService basicHuntingSummaryService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MooselikePermitListDTO getListDTO(final HarvestPermit permit, final int speciesCode, final Long viewedClubId) {
        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(permit, speciesCode);
        final GameSpecies species = speciesAmount.getGameSpecies();
        final String permitAreaExternalId = Optional.ofNullable(permit.getPermitDecision())
                .map(PermitDecision::getApplication)
                .map(HarvestPermitApplication::getArea)
                .map(HarvestPermitArea::getExternalId)
                .orElse(null);

        final ClubHuntingSummaryBasicInfoDTO huntingSummary = Optional.ofNullable(viewedClubId)
                .map(clubId -> basicHuntingSummaryService.getHuntingSummariesGroupedByClubId(permit, speciesCode).get(clubId))
                .orElse(null);

        return MooselikePermitListDTO.builder(speciesAmount)
                .withClubHuntingSummary(huntingSummary)
                .withViewedClubId(viewedClubId)
                .withPermitAreaExternalId(permitAreaExternalId)
                .withAmendmentPermits(speciesAmountRepository.countAmendmentPermitNumbersAndAmounts(permit, species))
                .withAllocatedCount(allocationRepository.countAllocatedPermitCount(permit, species))
                .withListLeadersButtonVisible(activeUserService.checkHasPermission(permit, LIST_LEADERS))
                .withHarvestCounts(harvestCountService.countHarvestsGroupingByClubId(permit, speciesCode).values())
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingClubPermitDTO getDTO(final HarvestPermit permit, final int speciesCode, final Long viewedClubId) {
        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(permit, speciesCode);
        final GameSpecies species = speciesAmount.getGameSpecies();

        final Map<Long, HarvestCountDTO> harvestCounts = harvestCountService.countHarvestsGroupingByClubId(permit, speciesCode);

        return HuntingClubPermitDTO.builder(speciesAmount)
                .withViewedClubId(viewedClubId)
                .withPermitHolderFinishedHunting(speciesAmount.isMooselikeHuntingFinished())
                .withHuntingFinishedByModeration(speciesAmount.isHuntingFinishedByModerator())
                .withCanModifyEndOfHunting(canModifyEndOfHunting(permit, speciesAmount))
                .withHarvestCounts(harvestCounts)
                .withAmendmentPermits(speciesAmountRepository.countAmendmentPermitNumbersAndAmounts(permit, species))
                .withTodo(moosePermitTodoService.listTodos(permit, species))
                .withAllocations(allocationRepository.getAllocationsIncludeMissingPartnerDTO(permit, species))
                .withPaymentBreakdown(mooselikePermitPriceService.getPartnerPriceBreakdown(species, harvestCounts))
                .withHuntingDayStatistics(huntingDayStatisticsService.calculateStatistics(speciesAmount))
                .withSummary(basicHuntingSummaryService.getHuntingSummariesGroupedByClubId(permit, speciesCode))
                .withPartnerAreas(harvestPermitPartnerAreaService.getApplicationAreasWithSize(permit))
                .build();
    }

    private boolean canModifyEndOfHunting(final HarvestPermit permit, final HarvestPermitSpeciesAmount speciesAmount) {
        return activeUserService.isModeratorOrAdmin() || (
                activeUserService.checkHasPermission(permit, CREATE_REMOVE_MOOSE_HARVEST_REPORT) &&
                        !harvestPermitLockedByDateService.isPermitLocked(speciesAmount));
    }

}
