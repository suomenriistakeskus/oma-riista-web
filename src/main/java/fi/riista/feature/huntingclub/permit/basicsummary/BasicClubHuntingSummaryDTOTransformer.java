package fi.riista.feature.huntingclub.permit.basicsummary;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.util.DtoUtil;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class BasicClubHuntingSummaryDTOTransformer
        extends ListTransformer<BasicClubHuntingSummary, BasicClubHuntingSummaryDTO> {

    @Resource
    private HuntingClubRepository huntingClubRepo;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Nonnull
    @Override
    protected List<BasicClubHuntingSummaryDTO> transform(@Nonnull final List<BasicClubHuntingSummary> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<BasicClubHuntingSummary, HuntingClub> summaryToClubMapping = getSummaryToClubMapping(list);
        final Function<BasicClubHuntingSummary, HarvestPermitSpeciesAmount> summaryToSpeciesAmountMapping =
                getSummaryToSpeciesAmountMapping(list);

        return list.stream().map(summary -> {
            final BasicClubHuntingSummaryDTO dto = new BasicClubHuntingSummaryDTO();
            DtoUtil.copyBaseFields(summary, dto);

            final HarvestPermitSpeciesAmount speciesAmount = summaryToSpeciesAmountMapping.apply(summary);
            // HarvestPermit is already fetched within species-amount query.
            final HarvestPermit harvestPermit = speciesAmount.getHarvestPermit();
            dto.setHarvestPermitId(harvestPermit.getId());
            dto.setPermitAreaSize(Objects.requireNonNull(harvestPermit.getPermitAreaSize(), "permitAreaSize is null"));

            // GameSpecies is already fetched within species-amount query.
            dto.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());

            final HuntingClub club = summaryToClubMapping.apply(summary);
            dto.setClubId(club.getId());
            dto.setNameFI(club.getNameFinnish());
            dto.setNameSV(club.getNameSwedish());

            dto.setModeratorOverridden(summary.isModeratorOverride());
            dto.setHuntingFinished(summary.isHuntingFinished());
            dto.setHuntingEndDate(summary.getHuntingEndDate());

            Optional.ofNullable(summary.getAreaSizeAndPopulation()).ifPresent(dto::withAreaSizeAndRemainingPopulation);

            dto.setNumberOfAdultMales(summary.getNumberOfAdultMales());
            dto.setNumberOfAdultFemales(summary.getNumberOfAdultFemales());
            dto.setNumberOfYoungMales(summary.getNumberOfYoungMales());
            dto.setNumberOfYoungFemales(summary.getNumberOfYoungFemales());
            dto.setNumberOfNonEdibleAdults(summary.getNumberOfNonEdibleAdults());
            dto.setNumberOfNonEdibleYoungs(summary.getNumberOfNonEdibleYoungs());
            return dto;
        }).collect(toList());
    }

    private Function<BasicClubHuntingSummary, HuntingClub> getSummaryToClubMapping(
            final Iterable<BasicClubHuntingSummary> summaries) {

        return CriteriaUtils.singleQueryFunction(summaries, BasicClubHuntingSummary::getClub, huntingClubRepo, false);
    }

    private Function<BasicClubHuntingSummary, HarvestPermitSpeciesAmount> getSummaryToSpeciesAmountMapping(
            final Iterable<BasicClubHuntingSummary> summaries) {

        // GameSpecies and HarvestPermit are fetched as well.

        final Specifications<HarvestPermitSpeciesAmount> joins = where(fetch(HarvestPermitSpeciesAmount_.gameSpecies))
                .and(fetch(HarvestPermitSpeciesAmount_.harvestPermit));
        return CriteriaUtils.singleQueryFunction(
                summaries, BasicClubHuntingSummary::getSpeciesAmount, speciesAmountRepo,
                joins, true);
    }

}
