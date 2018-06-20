package fi.riista.feature.organization.rhy.annualstats;

public enum RhyAnnualStatisticsState {

    // Statistics entity is created and being fulfilled by coordinator
    IN_PROGRESS,

    // Statistics entity is fulfilled by coordinator and being inspected and supplemented by moderator
    UNDER_INSPECTION,

    // Statistis entity is approved/locked
    APPROVED

}
