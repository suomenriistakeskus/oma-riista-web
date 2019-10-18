package fi.riista.feature.harvestpermit.statistics;

public enum MoosePermitStatisticsGroupBy {
    // Riistakeskusalue yhteensä
    RKA,

    // RHY:t listattuna riistakeskusalueittain
    RHY,

    // Luvansaajat riistanhoitoyhdistyksittäin
    RHY_PERMIT,

    // Hirvitalousalue yhteensä
    HTA,

    // Luvansaajat hirvitalousalueittain
    HTA_PERMIT,

    // RHY:t listattuna hirvitalousalueittain
    HTA_RHY;

    public boolean isGroupByPermit() {
        return this == RHY_PERMIT || this == HTA_PERMIT;
    }
}
