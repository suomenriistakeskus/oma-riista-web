package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.payment.MooselikePermitPriceService;
import fi.riista.feature.harvestpermit.violation.AmendmentPermitMatchHarvest;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceSynchronizer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.CREATE_REMOVE_MOOSE_HARVEST_REPORT;

@Component
public class EndOfMooselikePermitHuntingFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private MooselikePermitPriceService mooselikePermitPriceService;

    @Resource
    private PermitHarvestInvoiceSynchronizer permitHarvestInvoiceSynchronizer;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private HarvestCountService harvestCountService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(rollbackFor = IOException.class)
    public void endMooselikeHunting(final long harvestPermitId, final int gameSpeciesCode) throws IOException {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(
                harvestPermitId, CREATE_REMOVE_MOOSE_HARVEST_REPORT);
        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(
                harvestPermit, gameSpeciesCode);
        final GameSpecies gameSpecies = speciesAmount.getGameSpecies();

        if (!harvestPermit.isMooselikePermitType()) {
            throw new IllegalStateException("Can not end mooselike hunting for permit having type "
                    + harvestPermit.getPermitTypeCode());
        }

        if (!huntingFinishingService.allPartnersFinishedHunting(harvestPermit, gameSpeciesCode)) {
            throw new AllPartnersMustFinnishHuntingException(harvestPermit, gameSpecies);
        }

        final Map<Long, HarvestCountDTO> harvestCounts = harvestCountService.countHarvestsGroupingByClubId(
                speciesAmount.getHarvestPermit(), gameSpecies.getOfficialCode());

        final Map<String, Float> amendmentPermitNumbersAndAmounts =
                speciesAmountRepository.countAmendmentPermitNumbersAndAmounts(harvestPermit, gameSpecies);

        AmendmentPermitMatchHarvest.assertCountMatches(harvestPermit.getPermitNumber(),
                harvestCounts.values(), amendmentPermitNumbersAndAmounts);

        speciesAmount.assertMooselikeHuntingNotFinished();
        speciesAmount.setMooselikeHuntingFinished(true);

        if (activeUserService.isModeratorOrAdmin()) {
            speciesAmount.setHuntingFinishedByModerator(true);
        }

        if (harvestPermit.getPermitDecision() != null) {
            final BigDecimal paymentAmount = mooselikePermitPriceService
                    .getPartnerPriceBreakdown(gameSpecies, harvestCounts)
                    .getTotalPayment()
                    .getTotalPayment();

            permitHarvestInvoiceSynchronizer.synchronizeHarvestInvoice(speciesAmount, paymentAmount);
        }
    }

    @Transactional
    public void cancelEndMooselikeHunting(long harvestPermitId, int gameSpeciesCode) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(
                harvestPermitId, CREATE_REMOVE_MOOSE_HARVEST_REPORT);

        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepository
                .getOneByHarvestPermitAndSpeciesCode(harvestPermit, gameSpeciesCode);

        speciesAmount.setMooselikeHuntingFinished(false);

        if (harvestPermit.getPermitDecision() != null) {
            permitHarvestInvoiceSynchronizer.cancelInvoice(speciesAmount);
        }
    }
}
