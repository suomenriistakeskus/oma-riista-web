package fi.riista.feature.huntingclub.statistics;

import fi.riista.feature.gamediary.GameSpeciesDTO;

import java.util.List;

public class HuntingClubHarvestStatisticsDTO {

    public static class SummaryRow {
        private final GameSpeciesDTO species;
        private final long count;

        public SummaryRow(final GameSpeciesDTO species, final long count) {
            this.species = species;
            this.count = count;
        }

        public GameSpeciesDTO getSpecies() {
            return species;
        }

        public long getCount() {
            return count;
        }
    }

    private final List<SummaryRow> items;

    public HuntingClubHarvestStatisticsDTO(final List<SummaryRow> items) {
        this.items = items;
    }

    public List<SummaryRow> getItems() {
        return items;
    }

}
