package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class HuntingFinishingService {

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private ClubHuntingSummaryBasicInfoService clubHuntingSummaryBasicInfoService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MooseHuntingSummary markUnfinished(final MooseHuntingSummary summary) {
        final HarvestPermitSpeciesAmount mooseAmount =
                harvestPermitSpeciesAmountRepository.getMooseAmount(summary.getHarvestPermit());

        mooseAmount.assertMooselikeHuntingNotFinished();

        summary.setHuntingFinished(false);

        // Flush needed to have DTO's rev updated.
        return mooseHuntingSummaryRepository.saveAndFlush(summary);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public BasicClubHuntingSummary markUnfinished(final BasicClubHuntingSummary summary) {
        summary.getSpeciesAmount().assertMooselikeHuntingNotFinished();

        summary.setHuntingFinished(false);

        // Flush needed to have DTO's rev updated.
        return basicHuntingSummaryRepository.saveAndFlush(summary);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasPermitPartnerFinishedHunting(@Nonnull final HuntingClubGroup group) {
        requireNonNull(group);

        final int speciesCode = group.getSpecies().getOfficialCode();
        final long clubId = group.getParentOrganisation().getId();

        return Optional.ofNullable(group.getHarvestPermit())
                .map(permit -> clubHuntingSummaryBasicInfoService
                        .getHuntingSummariesGroupedByClubId(permit, speciesCode)
                        .get(clubId))
                .map(ClubHuntingSummaryBasicInfoDTO::isHuntingFinished)
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean allPartnersFinishedHunting(@Nonnull final HarvestPermit permit, final int speciesCode) {
        return clubHuntingSummaryBasicInfoService
                .getHuntingSummariesGroupedByClubId(permit, speciesCode).values().stream()
                .allMatch(ClubHuntingSummaryBasicInfoDTO::isHuntingFinished);
    }
}
