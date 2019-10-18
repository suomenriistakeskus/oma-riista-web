package fi.riista.feature.harvestpermit.statistics;

import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.PermitAndLocationId;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class MoosePermitStatisticsBuilder {
    private final MoosePermitStatisticsI18n i18n;
    private final List<PermitData> permitData;
    private final Map<PermitAndLocationId, HarvestCountDTO> harvestData;

    public MoosePermitStatisticsBuilder(final MoosePermitStatisticsI18n i18n) {
        this.i18n = requireNonNull(i18n);
        this.permitData = new LinkedList<>();
        this.harvestData = new HashMap<>();
    }

    public void addPermit(final @Nonnull MoosePermitStatisticsPermitInfo permitInfo,
                          final @Nonnull MoosePermitStatisticsAmountDTO permitAmount,
                          final @Nonnull MoosePermitStatisticsAreaAndPopulation areaAndPopulation) {
        this.permitData.add(new PermitData(permitInfo, permitAmount, areaAndPopulation));
    }

    public void addHarvestCount(final @Nonnull PermitAndLocationId key, HarvestCountDTO count) {
        this.harvestData.put(key, count);
    }

    public void addHarvestCounts(final @Nonnull Map<PermitAndLocationId, HarvestCountDTO> counts) {
        this.harvestData.putAll(counts);
    }

    public List<MoosePermitStatisticsDTO> build(final MoosePermitStatisticsGroupBy groupBy, final boolean includeGrandTotal) {
        final Collection<MoosePermitStatisticsDTO> resultList = groupResults(groupBy);
        final Stream<MoosePermitStatisticsDTO> grandTotal = includeGrandTotal
                ? Stream.of(createGrandTotal(resultList))
                : Stream.empty();

        return Stream.concat(grandTotal, resultList.stream().sorted(createComparator())).collect(toList());
    }

    @Nonnull
    private MoosePermitStatisticsDTO createGrandTotal(final Collection<MoosePermitStatisticsDTO> resultList) {
        return new MoosePermitStatisticsDTO(
                i18n.getSpeciesName(),
                HarvestCountDTO.createStatisticTotal(resultList),
                MoosePermitStatisticsAmountDTO.createTotal(
                        F.mapNonNullsToList(permitData, PermitData::getPermitAmount)),
                MoosePermitStatisticsAreaAndPopulation.createTotal(
                        F.mapNonNullsToList(permitData, PermitData::getAreaAndPopulation)));
    }

    private List<MoosePermitStatisticsDTO> groupResults(final MoosePermitStatisticsGroupBy groupBy) {
        final Map<PermitAndLocationId, GroupedHarvestCounts> groupedHarvestCounts = harvestData.entrySet().stream()
                .collect(groupingBy(entry -> entry.getKey().getGroupByValue(groupBy), collectingAndThen(toList(),
                        entryList -> new GroupedHarvestCounts(
                                F.mapNonNullsToSet(entryList, entry -> entry.getKey().getPermitId()),
                                F.mapNonNullsToList(entryList, Map.Entry::getValue)))));

        return groupedHarvestCounts.entrySet().stream().map(entry -> {
            final PermitAndLocationId groupKey = entry.getKey();
            final Set<Long> groupPermitIds = entry.getValue().getPermitIds();
            final PermitData totalPermitData = getTotalPermitData(groupBy.isGroupByPermit(), groupPermitIds);

            return new MoosePermitStatisticsDTO(
                    i18n.getSpeciesName(),
                    i18n.getRhyName(groupKey),
                    i18n.getRkaName(groupKey),
                    i18n.getHtaName(groupKey),
                    totalPermitData.getPermitInfo(),
                    HarvestCountDTO.createTotal(entry.getValue().getHarvestCounts()),
                    totalPermitData.getPermitAmount(),
                    totalPermitData.getAreaAndPopulation());
        }).collect(toList());
    }

    @Nonnull
    private PermitData getTotalPermitData(final boolean includePermitInfo, final Set<Long> groupPermitIds) {
        final List<PermitData> groupPermitData = F.filterToList(this.permitData,
                permit -> groupPermitIds.contains(permit.getPermitId()));
        final List<MoosePermitStatisticsAmountDTO> amounts =
                F.mapNonNullsToList(groupPermitData, PermitData::getPermitAmount);
        final List<MoosePermitStatisticsAreaAndPopulation> areaAndPopulationList =
                F.mapNonNullsToList(groupPermitData, PermitData::getAreaAndPopulation);
        final MoosePermitStatisticsPermitInfo permitInfo = includePermitInfo && groupPermitData.size() == 1
                ? groupPermitData.iterator().next().getPermitInfo()
                : null;

        return new PermitData(permitInfo,
                MoosePermitStatisticsAmountDTO.createTotal(amounts),
                MoosePermitStatisticsAreaAndPopulation.createTotal(areaAndPopulationList));
    }

    private Comparator<MoosePermitStatisticsDTO> createComparator() {
        final Comparator<MoosePermitStatisticsDTO> permitNumber = comparing(MoosePermitStatisticsDTO::getPermitNumber, nullsFirst(naturalOrder()));
        final Comparator<MoosePermitStatisticsDTO> permitHolder = comparing(a -> i18n.getAnyTranslation(a.getPermitHolder()), nullsFirst(naturalOrder()));
        return permitNumber.thenComparing(permitHolder);
    }

    private static class GroupedHarvestCounts {
        private final Set<Long> permitIds;
        private final List<HarvestCountDTO> harvestCounts;

        private GroupedHarvestCounts(final Set<Long> permitIds, final List<HarvestCountDTO> harvestCounts) {
            this.permitIds = permitIds;
            this.harvestCounts = harvestCounts;
        }

        public Set<Long> getPermitIds() {
            return permitIds;
        }

        public List<HarvestCountDTO> getHarvestCounts() {
            return harvestCounts;
        }
    }

    private static class PermitData {
        private final MoosePermitStatisticsPermitInfo permitInfo;
        private final MoosePermitStatisticsAmountDTO permitAmount;
        private final MoosePermitStatisticsAreaAndPopulation areaAndPopulation;

        private PermitData(final MoosePermitStatisticsPermitInfo permitInfo,
                           final @Nonnull MoosePermitStatisticsAmountDTO permitAmount,
                           final @Nonnull MoosePermitStatisticsAreaAndPopulation areaAndPopulation) {
            this.permitInfo = permitInfo;
            this.permitAmount = requireNonNull(permitAmount);
            this.areaAndPopulation = requireNonNull(areaAndPopulation);
        }

        public Long getPermitId() {
            return permitInfo != null ? permitInfo.getPermitId() : null;
        }

        public MoosePermitStatisticsPermitInfo getPermitInfo() {
            return permitInfo;
        }

        public MoosePermitStatisticsAmountDTO getPermitAmount() {
            return permitAmount;
        }

        public MoosePermitStatisticsAreaAndPopulation getAreaAndPopulation() {
            return areaAndPopulation;
        }
    }

}


















