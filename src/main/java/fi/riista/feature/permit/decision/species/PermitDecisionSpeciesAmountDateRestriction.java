package fi.riista.feature.permit.decision.species;

import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class PermitDecisionSpeciesAmountDateRestriction {
    private final String permitTypeCode;
    private final int decisionYear;
    private final int speciesYear;

    public static PermitDecisionSpeciesAmountDateRestriction create(final @Nonnull PermitDecisionSpeciesAmount spa) {
        requireNonNull(spa);
        final PermitDecision permitDecision = requireNonNull(spa.getPermitDecision());

        return new PermitDecisionSpeciesAmountDateRestriction(
                permitDecision.getPermitTypeCode(), permitDecision.getDecisionYear(), spa.getPermitYear());
    }

    public PermitDecisionSpeciesAmountDateRestriction(final String permitTypeCode,
                                                      final int decisionYear,
                                                      final int speciesYear) {
        this.permitTypeCode = permitTypeCode;
        this.decisionYear = decisionYear;
        this.speciesYear = speciesYear;
    }

    public LocalDate resolveMinBeginDate() {
        switch (permitTypeCode) {
            case PermitTypeCode.MOOSELIKE:
                return DateUtil.huntingYearBeginDate(decisionYear);
            case PermitTypeCode.MOOSELIKE_AMENDMENT:
                return DateUtil.huntingYearBeginDate(DateUtil.huntingYear());
            case PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD:
                return new LocalDate(speciesYear, 1, 1);
        }

        // Species amount can not start before decision has been made
        return new LocalDate(decisionYear, 1, 1);
    }

    public LocalDate resolveMaxBeginDate() {
        switch (permitTypeCode) {
            case PermitTypeCode.MOOSELIKE:
                return DateUtil.huntingYearEndDate(decisionYear);
            case PermitTypeCode.MOOSELIKE_AMENDMENT:
                return DateUtil.huntingYearEndDate(DateUtil.huntingYear());
            case PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD:
                return new LocalDate(speciesYear, 12, 31);
        }

        return null;
    }

    public void assertValid(final @Nonnull PermitDecisionSpeciesAmountDTO dto) {
        if (!isValid(dto)) {
            throw new IllegalArgumentException(String.format("Date (%s) not in interval %s - %s",
                    dto.getBeginDate(), resolveMinBeginDate(), resolveMaxBeginDate()));
        }
    }

    boolean isValid(final PermitDecisionSpeciesAmountDTO dto) {
        final LocalDate minBeginDate = resolveMinBeginDate();
        final LocalDate maxBeginDate = resolveMaxBeginDate();

        return DateUtil.overlapsInclusive(minBeginDate, maxBeginDate, dto.getBeginDate());
    }
}
