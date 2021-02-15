package fi.riista.feature.organization.rhy.subsidy.excel;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.RhySubsidyStage5DTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculationStage5DTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class RhySubsidyExcelModel {

    public static List<RkaSubsidyAllocation> groupRhyAllocationsByRka(
            @Nonnull final List<RhySubsidyStage5DTO> allRhyAllocations) {

        requireNonNull(allRhyAllocations);

        final Map<String, OrganisationNameDTO> parentOrganisationIndex = new HashMap<>();

        allRhyAllocations.forEach(allocation -> {
            final OrganisationNameDTO rka = allocation.getRka();
            final String rkaCode = rka.getOfficialCode();

            if (!parentOrganisationIndex.containsKey(rkaCode)) {
                parentOrganisationIndex.put(rkaCode, rka);
            }
        });

        final SortedMap<String, List<RhySubsidyStage5DTO>> rhyAllocationsGroupedByRkaCode = allRhyAllocations
                .stream()
                .collect(groupingBy(dto -> dto.getRka().getOfficialCode(), TreeMap::new, toList()));

        return rhyAllocationsGroupedByRkaCode
                .entrySet()
                .stream()
                .map(entry -> {
                    final OrganisationNameDTO rka = parentOrganisationIndex.get(entry.getKey());
                    return new RkaSubsidyAllocation(rka, entry.getValue());
                })
                .collect(toList());
    }

    public static TotalSubsidyAllocation aggregate(@Nonnull final List<RkaSubsidyAllocation> rkaAllocations) {
        requireNonNull(rkaAllocations);

        final List<SubsidyCalculationStage5DTO> rkaSummaries = F.mapNonNullsToList(rkaAllocations, a -> a.summary);

        return new TotalSubsidyAllocation(SubsidyCalculationStage5DTO.aggregate(rkaSummaries));
    }

    public interface AggregatedSubsidyAllocation {

        SubsidyCalculationStage5DTO getSummary();

        boolean isSummaryOfAllRhys();
    }

    public static class RkaSubsidyAllocation implements AggregatedSubsidyAllocation {

        private final OrganisationNameDTO rka;
        private final SubsidyCalculationStage5DTO summary;
        private final List<RhySubsidyStage5DTO> rhyAllocations;

        private RkaSubsidyAllocation(@Nonnull final OrganisationNameDTO rka,
                                     @Nonnull final List<RhySubsidyStage5DTO> rhyAllocations) {

            this.rka = requireNonNull(rka);

            this.rhyAllocations = requireNonNull(rhyAllocations)
                    .stream()
                    .sorted(comparing(dto -> dto.getRhy().getOfficialCode()))
                    .collect(toList());

            final List<SubsidyCalculationStage5DTO> calculations =
                    F.mapNonNullsToList(rhyAllocations, RhySubsidyStage5DTO::getCalculation);

            this.summary = SubsidyCalculationStage5DTO.aggregate(calculations);
        }

        @Override
        public boolean isSummaryOfAllRhys() {
            return false;
        }

        @Override
        public SubsidyCalculationStage5DTO getSummary() {
            return summary;
        }

        public OrganisationNameDTO getRka() {
            return rka;
        }

        public List<RhySubsidyStage5DTO> getRhyAllocations() {
            return rhyAllocations;
        }
    }

    public static class TotalSubsidyAllocation implements AggregatedSubsidyAllocation {

        private final SubsidyCalculationStage5DTO summary;

        private TotalSubsidyAllocation(@Nonnull final SubsidyCalculationStage5DTO summary) {
            this.summary = requireNonNull(summary);
        }

        @Override
        public boolean isSummaryOfAllRhys() {
            return true;
        }

        @Override
        public SubsidyCalculationStage5DTO getSummary() {
            return summary;
        }
    }
}
