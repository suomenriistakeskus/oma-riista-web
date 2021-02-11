package fi.riista.feature.dashboard;

import fi.riista.util.LocalisedString;

public class PermitInfoDTO {

    public static final PermitInfoDTO create(final long permitId, final LocalisedString rhy, final LocalisedString rka){
        return new PermitInfoDTO(permitId, rhy, rka);
    }

    private final long permitId;
    private final LocalisedString rhy;
    private final LocalisedString rka;

    private PermitInfoDTO(final long permitId, final LocalisedString rhy, final LocalisedString rka) {
        this.permitId = permitId;
        this.rhy = rhy;
        this.rka = rka;
    }

    public long getPermitId() {
        return permitId;
    }

    public LocalisedString getRhy() {
        return rhy;
    }

    public LocalisedString getRka() {
        return rka;
    }
}
