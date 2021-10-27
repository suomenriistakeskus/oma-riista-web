package fi.riista.feature.largecarnivorereport;

import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedDTO;

import java.util.List;

public class LargeCarnivoreOtherwiseDeceasedDTO {

    final OtherwiseDeceasedDTO otherwiseDeceased;
    final boolean reindeerArea;

    public LargeCarnivoreOtherwiseDeceasedDTO(final OtherwiseDeceasedDTO otherwiseDeceased,
                                              final boolean reindeerArea) {
        this.otherwiseDeceased = otherwiseDeceased;
        this.reindeerArea = reindeerArea;
    }

    public OtherwiseDeceasedDTO getOtherwiseDeceased() {
        return otherwiseDeceased;
    }

    public boolean isReindeerArea() {
        return reindeerArea;
    }
}
