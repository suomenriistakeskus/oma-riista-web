package fi.riista.feature.organization.rhy.annualstats;

public enum RhyAnnualStatisticsState {

    // Statistics entity is created but coordinator has not done any changes
    NOT_STARTED,

    // Statistics entity is being fulfilled by coordinator
    IN_PROGRESS,

    // Statistics entity is fulfilled by coordinator and being inspected and supplemented by moderator
    UNDER_INSPECTION,

    // Statistis entity is approved/locked
    APPROVED

}
