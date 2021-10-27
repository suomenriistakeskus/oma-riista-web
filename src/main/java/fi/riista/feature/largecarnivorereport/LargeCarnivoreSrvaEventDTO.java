package fi.riista.feature.largecarnivorereport;

import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;

public class LargeCarnivoreSrvaEventDTO {

    public static LargeCarnivoreSrvaEventDTO create(final SrvaEventDTO event,
                                                    final RiistanhoitoyhdistysDTO rhy,
                                                    final OrganisationNameDTO rka) {
        return new LargeCarnivoreSrvaEventDTO(event, rhy, rka);
    }

    private final SrvaEventDTO event;
    private final RiistanhoitoyhdistysDTO rhy;
    private final OrganisationNameDTO rka;

    private LargeCarnivoreSrvaEventDTO(final SrvaEventDTO event,
                                      final RiistanhoitoyhdistysDTO rhy,
                                      final OrganisationNameDTO rka) {
        this.event = event;
        this.rhy = rhy;
        this.rka = rka;
    }

    public SrvaEventDTO getEvent() {
        return event;
    }

    public RiistanhoitoyhdistysDTO getRhy() {
        return rhy;
    }

    public OrganisationNameDTO getRka() {
        return rka;
    }
}
