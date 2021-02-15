package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class RhySubsidyRoundingStage4DTO {

    private final String rhyCode;
    private final SubsidyRoundingDTO roundingResult;

    public RhySubsidyRoundingStage4DTO(@Nonnull final String rhyCode,
                                       @Nonnull final SubsidyRoundingDTO roundingResult) {

        this.rhyCode = requireNonNull(rhyCode);
        this.roundingResult = requireNonNull(roundingResult);
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public SubsidyRoundingDTO getRoundingResult() {
        return roundingResult;
    }
}
