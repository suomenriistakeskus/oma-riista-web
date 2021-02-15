package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.MergedRhyMapping;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameDTO;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_SUBSIDY_YEAR;
import static fi.riista.util.NumberUtils.nullableSum;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class RhySubsidyMergeResolver {

    private final int subsidyYear;
    private final Map<String, RiistanhoitoyhdistysNameDTO> rhyNameIndex;

    public RhySubsidyMergeResolver(final int subsidyYear, @Nonnull final List<RiistanhoitoyhdistysNameDTO> rhyNames) {
        this.subsidyYear = subsidyYear;

        if (subsidyYear < FIRST_SUBSIDY_YEAR) {
            throw new IllegalArgumentException("subsidyYear must be at least " + FIRST_SUBSIDY_YEAR);
        }

        this.rhyNameIndex = F.index(rhyNames, RiistanhoitoyhdistysNameDTO::getRhyOfficialCode);
    }

    public List<AnnualStatisticsExportDTO> mergeStatistics(@Nonnull final List<AnnualStatisticsExportDTO> allRhyStatistics) {
        requireNonNull(allRhyStatistics);

        final Map<String, AnnualStatisticsExportDTO> statisticsIndex =
                F.index(allRhyStatistics, dto -> dto.getOrganisation().getOfficialCode());

        final Map<String, AnnualStatisticsExportDTO> mergedStatisticsIndex = MergedRhyMapping
                .transformMerged(statisticsIndex, subsidyYear, (newRhyCode, statisticsToMerge) -> {

                    return ofNullable(rhyNameIndex.get(newRhyCode))
                            .map(newRhyNameDTO -> merge(newRhyNameDTO, statisticsToMerge))
                            .orElseThrow(() -> new IllegalStateException(format(
                                    "RHY with officialCode %s does not exist at year %d", newRhyCode, subsidyYear)));
                });

        return mergedStatisticsIndex.values().stream().collect(toList());
    }

    private static AnnualStatisticsExportDTO merge(final RiistanhoitoyhdistysNameDTO nameInfo,
                                                   final Collection<AnnualStatisticsExportDTO> statisticsToMerge) {

        final LocalisedString rhyName = nameInfo.getRhyName();
        final LocalisedString rkaName = nameInfo.getRkaName();

        final OrganisationNameDTO rhy = new OrganisationNameDTO();
        rhy.setId(nameInfo.getRhyId());
        rhy.setOfficialCode(nameInfo.getRhyOfficialCode());
        rhy.setNameFI(rhyName.getFinnish());
        rhy.setNameSV(rhyName.getSwedish());

        final OrganisationNameDTO rka = new OrganisationNameDTO();
        rka.setId(nameInfo.getRkaId());
        rka.setOfficialCode(nameInfo.getRkaOfficialCode());
        rka.setNameFI(rkaName.getFinnish());
        rka.setNameSV(rkaName.getSwedish());

        final AnnualStatisticsExportDTO mergeResult = AnnualStatisticsExportDTO.aggregate(statisticsToMerge);
        mergeResult.setOrganisation(rhy);
        mergeResult.setParentOrganisation(rka);
        return mergeResult;
    }

    public List<AnnualStatisticsExportDTO> combine(final List<AnnualStatisticsExportDTO> previousYearStats,
                                                   final List<AnnualStatisticsExportDTO> stats) {

        final Function<AnnualStatisticsExportDTO, String> officialCodeExtractor =
                dto -> dto.getOrganisation().getOfficialCode();

        final Map<String, AnnualStatisticsExportDTO> previousYearMap = F.index(previousYearStats, officialCodeExtractor);
        final Map<String, AnnualStatisticsExportDTO> map = F.index(stats, officialCodeExtractor);

        final Map<String, AnnualStatisticsExportDTO> resultMap = new HashMap<>(map);
        previousYearMap.forEach((k, v) ->
                resultMap.merge(k, v, (dto1, dto2) ->
                        ofNullable(rhyNameIndex.get(dto1.getOrganisation().getOfficialCode()))
                                .map(nameInfo -> merge(nameInfo, Arrays.asList(dto1, dto2)))
                                .orElseThrow(NotFoundException::new)));

        return resultMap.values().stream()
                .sorted(comparing(officialCodeExtractor))
                .collect(toList());
    }


    public PreviouslyGrantedSubsidiesDTO mergePreviouslyGrantedSubsidies(
            @Nonnull final PreviouslyGrantedSubsidiesDTO grantedSubsidies) {

        requireNonNull(grantedSubsidies);

        final Map<String, BigDecimal> subsidyIndexOfLastYear = grantedSubsidies.getRhyCodeToSubsidyGrantedLastYear();

        final Map<String, BigDecimal> mergedSubsidyIndexOfLastYear = MergedRhyMapping.transformMerged(
                subsidyIndexOfLastYear, subsidyYear, (newRhyCode, amounts) -> nullableSum(amounts.stream()));

        return new PreviouslyGrantedSubsidiesDTO(
                mergedSubsidyIndexOfLastYear, grantedSubsidies.getRhyCodeToSubsidyGrantedInFirstBatchOfCurrentYear());
    }
}
