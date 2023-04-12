package fi.riista.feature.huntingclub.statistics.gamestatistics;

import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameStatisticsDTO {

    //Dataset keys for DeerCensus and Moose statistics
    public static final String WHITE_TAIL_DEERS = "whiteTailDeers";
    public static final String ROE_DEERS = "roeDeers";
    public static final String FALLOW_DEERS = "fallowDeers";

    //Dataset keys for Deer statistics
    public static final String WHITE_TAIL_DEER_REMAINING_POPULATION_IN_TOTAL_AREA = "whiteTailDeerRemainingPopulationInTotalArea";
    public static final String WHITE_TAIL_DEER_REMAINING_POPULATION_IN_EFFECTIVE_AREA = "whiteTailDeerRemainingPopulationInEffectiveArea";

    //Dataset keys for Moose statistics
    public static final String DROWNED_MOOSES = "drownedMooses";
    public static final String MOOSES_KILLED_BY_BEAR = "moosesKilledByBear";
    public static final String MOOSES_KILLED_BY_WOLF = "moosesKilledByWolf";
    public static final String MOOSES_KILLED_IN_TRAFFIC_ACCIDENT = "moosesKilledInTrafficAccident";
    public static final String MOOSES_KILLED_BY_POACHING = "moosesKilledByPoaching";
    public static final String MOOSES_KILLED_IN_RUT_FIGHT = "moosesKilledInRutFight";
    public static final String STARVED_MOOSES = "starvedMooses";
    public static final String MOOSES_DECEASED_BY_OTHER_REASON = "moosesDeceasedByOtherReason";
    public static final String TOTAL_DEAD_MOOSES = "totalDeadMooses";
    public static final String REMAINING_MOOSES_IN_TOTAL_AREA = "remainingMoosesInTotalArea";
    public static final String REMAINING_MOOSES_IN_EFFECTIVE_AREA = "remainingMoosesInEffectiveArea";
    public static final String WILD_FOREST_REINDEERS = "wildForestReindeers";
    public static final String BEAVERS_AMOUNT_OF_INHABITED_WINTER_NESTS = "beaversAmountOfInhabitedWinterNests";
    public static final String BEAVERS_HARVEST_AMOUNT = "beaversHarvestAmount";
    public static final String BEAVERS_AREA_OF_DAMAGE = "beaversAreaOfDamage";
    public static final String BEAVERS_AREA_OCCUPIED_BY_WATER = "beaversAreaOccupiedByWater";
    public static final String WILD_BOARS_ESTIMATED_AMOUNT_OF_SPECIMENS = "wildBoarsEstimatedAmountOfSpecimens";
    public static final String WILD_BOARS_ESTIMATED_AMOUNT_OF_SOW_WITH_PIGLETS = "wildBoarsEstimatedAmountOfSowWithPiglets";

    private List<LocalDate> timestamps = new ArrayList<>();
    private Map<String, List<Integer>> datasets = new HashMap<>();

    private boolean containsAnnuallyCombinedData;

    public GameStatisticsDTO() {}

    public GameStatisticsDTO appendTimestampForYear(int year) {
        timestamps.add(new LocalDate(year, 1, 1));
        return this;
    }

    public GameStatisticsDTO appendData(@NotNull String datasetKey, Integer value) {
        if (!datasets.containsKey(datasetKey)) {
            datasets.put(datasetKey, new ArrayList<>());
        }
        datasets.get(datasetKey).add(Optional.ofNullable(value).orElse(0));
        return this;
    }

    public void combineDataAnnually() {
        for (int currentIndex = 0; currentIndex < timestamps.size() - 1; currentIndex++) {
            int nextIndex = currentIndex + 1;
            int yearAtCurrentIndex = timestamps.get(currentIndex).getYear();
            int yearAtNextIndex = timestamps.get(nextIndex).getYear();

            if (yearAtCurrentIndex == yearAtNextIndex) {
                containsAnnuallyCombinedData = true;
                timestamps.remove(nextIndex);

                for (String key: datasets.keySet()) {
                    List<Integer> dataset = datasets.get(key);
                    Integer sum = dataset.get(currentIndex) + dataset.get(nextIndex);
                    dataset.set(currentIndex, sum);
                    dataset.remove(nextIndex);
                }
                currentIndex--;
            }
        }
    }

    public List<LocalDate> getTimestamps() {
        return timestamps;
    }

    public Map<String, List<Integer>> getDatasets() {
        return datasets;
    }

    public boolean containsAnnuallyCombinedData() {
        return containsAnnuallyCombinedData;
    }

    @AssertTrue(message = "timestamps and datasets length must match")
    public boolean isTimestampAndDatasetLengthMatch() {
        for (List<Integer> dataset: datasets.values()) {
            if (dataset.size() != timestamps.size()) {
                return false;
            }
        }
        return true;
    }
}
