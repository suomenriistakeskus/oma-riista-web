package fi.riista.feature.permit.decision.derogation.pdf;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Locale;

import static fi.riista.util.DateUtil.today;

public class AnnualRenewalPermitPdfModelDTO {
    private final LocalDate renewalDate;
    private final LocalDate decisionDate;
    private final Integer decisionNumber;
    private final int permitYear;
    private final String permitNumber;

    private final Locale decisionLocale;


    public AnnualRenewalPermitPdfModelDTO(final PermitDecision decision,
                                          final HarvestPermit permit,
                                          final DateTime publishDate) {
        this.renewalDate = DateUtil.toLocalDateNullSafe(permit.getCreationTime());
        this.decisionDate = publishDate.toLocalDate();
        this.decisionNumber = decision.getDecisionNumber();
        this.decisionLocale = decision.getLocale();
        this.permitYear = permit.getPermitYear();
        this.permitNumber = permit.getPermitNumber();
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public Integer getDecisionNumber() {
        return decisionNumber;
    }

    public int getPermitYear() {
        return permitYear;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public Locale getDecisionLocale() {
        return decisionLocale;
    }
}
