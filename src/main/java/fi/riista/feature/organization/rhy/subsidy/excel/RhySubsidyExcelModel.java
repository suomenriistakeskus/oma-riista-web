package fi.riista.feature.organization.rhy.subsidy.excel;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationStage4DTO;
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

    public static List<AggregatedSubsidyAllocation> groupRhyAllocationsByRka(
            @Nonnull final List<SubsidyAllocationStage4DTO> allRhyAllocations) {

        requireNonNull(allRhyAllocations);

        final Map<String, OrganisationNameDTO> parentOrganisationIndex = new HashMap<>();

        allRhyAllocations.forEach(allocation -> {
            final OrganisationNameDTO rka = allocation.getParentOrganisation();
            final String rkaCode = rka.getOfficialCode();

            if (!parentOrganisationIndex.containsKey(rkaCode)) {
                parentOrganisationIndex.put(rkaCode, rka);
            }
        });

        final SortedMap<String, List<SubsidyAllocationStage4DTO>> rhyAllocationsGroupedByRkaCode = allRhyAllocations
                .stream()
                .collect(groupingBy(dto -> dto.getParentOrganisation().getOfficialCode(), TreeMap::new, toList()));

        return rhyAllocationsGroupedByRkaCode.entrySet()
                .stream()
                .map(entry -> {
                    final OrganisationNameDTO rka = parentOrganisationIndex.get(entry.getKey());
                    return AggregatedSubsidyAllocation.createRkaSummary(entry.getValue(), rka);
                })
                .collect(toList());
    }

    public static class AggregatedSubsidyAllocation {

        final List<SubsidyAllocationStage4DTO> allocations;
        final SubsidyAllocationStage4DTO summary;
        final boolean isSummaryOfAllRhys;

        public static AggregatedSubsidyAllocation createRkaSummary(@Nonnull final List<SubsidyAllocationStage4DTO> allocations,
                                                                   @Nonnull final OrganisationNameDTO rka) {

            requireNonNull(rka);

            return new AggregatedSubsidyAllocation(
                    allocations, SubsidyAllocationStage4DTO.aggregate(allocations, rka), false);
        }

        public static AggregatedSubsidyAllocation aggregate(@Nonnull final Iterable<AggregatedSubsidyAllocation> rkaList) {
            requireNonNull(rkaList);

            final List<SubsidyAllocationStage4DTO> rkaSummaries = F.mapNonNullsToList(rkaList, a -> a.summary);

            final SubsidyAllocationStage4DTO summaryOfAll = SubsidyAllocationStage4DTO.aggregate(rkaSummaries, null);

            return new AggregatedSubsidyAllocation(rkaSummaries, summaryOfAll, true);
        }

        private AggregatedSubsidyAllocation(@Nonnull final List<SubsidyAllocationStage4DTO> allocations,
                                            @Nonnull final SubsidyAllocationStage4DTO summary,
                                            final boolean isSummaryOfAllRhys) {

            this.allocations = requireNonNull(allocations)
                    .stream()
                    .sorted(comparing(dto -> dto.getOrganisation().getOfficialCode()))
                    .collect(toList());

            this.summary = requireNonNull(summary);
            this.isSummaryOfAllRhys = isSummaryOfAllRhys;
        }
    }
}
