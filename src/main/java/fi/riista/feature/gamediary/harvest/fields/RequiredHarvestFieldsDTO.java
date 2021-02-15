package fi.riista.feature.gamediary.harvest.fields;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class RequiredHarvestFieldsDTO {

    private final RequiredHarvestReportFieldsDTO report;
    private final RequiredHarvestSpecimenFieldsDTO specimen;

    public RequiredHarvestFieldsDTO(@Nonnull final RequiredHarvestReportFieldsDTO reportFields,
                                    @Nonnull final RequiredHarvestSpecimenFieldsDTO specimenFields) {

        this.report = requireNonNull(reportFields);
        this.specimen = requireNonNull(specimenFields);
    }

    public RequiredHarvestReportFieldsDTO getReport() {
        return report;
    }

    public RequiredHarvestSpecimenFieldsDTO getSpecimen() {
        return specimen;
    }
}
