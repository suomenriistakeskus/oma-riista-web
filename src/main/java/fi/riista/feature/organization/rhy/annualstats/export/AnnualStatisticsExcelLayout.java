package fi.riista.feature.organization.rhy.annualstats.export;

public enum AnnualStatisticsExcelLayout {

    NORMAL,

    // In tranposed layout the data is column-oriented i.e. the cells belonging to a single record
    // are written into one column (instead of one row).
    TRANSPOSED_WITH_MULTIPLE_SHEETS,

    // RHY objects are grouped by RiistakeskuksenAlue
    WITH_RKA_GROUPING

}
