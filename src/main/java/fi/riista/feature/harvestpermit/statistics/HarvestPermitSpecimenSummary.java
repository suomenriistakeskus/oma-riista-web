package fi.riista.feature.harvestpermit.statistics;

import com.google.common.collect.Maps;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;

import java.util.Map;

public final class HarvestPermitSpecimenSummary {
    public static Map<GameSpecies, HarvestPermitSpecimenSummary> create(final Iterable<Harvest> harvests) {
        final Map<GameSpecies, HarvestPermitSpecimenSummary> map = Maps.newHashMap();

        for (final Harvest h : harvests) {
            final GameSpecies g = h.getSpecies();
            if (!map.containsKey(g)) {
                map.put(g, new HarvestPermitSpecimenSummary());
            }
            final HarvestPermitSpecimenSummary sum = map.get(g);
            sum.addTotal(h.getAmount());
            for (HarvestSpecimen sp : h.getSortedSpecimens()) {
                sum.update(sp.getAge());
                sum.update(sp.getGender());
            }
        }

        return map;
    }

    private final Map<Object, Integer> counts = Maps.newHashMap();
    private int total = 0;

    public void update(final Object key) {
        if (key == null) {
            return;
        }
        if (!counts.containsKey(key)) {
            counts.put(key, 0);
        }
        counts.put(key, counts.get(key) + 1);
    }

    public void addTotal(int count) {
        this.total += count;
    }

    private int get(Object key) {
        return counts.getOrDefault(key, 0);
    }

    public int getTotal() {
        return total;
    }

    public int getGenderFemale() {
        return get(GameGender.FEMALE);
    }

    public int getGenderMale() {
        return get(GameGender.MALE);
    }

    public int getGenderUnknown() {
        return get(GameGender.UNKNOWN);
    }

    public int getAgeAdult() {
        return get(GameAge.ADULT);
    }

    public int getAgeYoung() {
        return get(GameAge.YOUNG);
    }

    public int getAgeUnknown() {
        return get(GameAge.UNKNOWN);
    }
}
