package fi.riista.feature.largecarnivorereport;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecision.DecisionType;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class LargeCarnivorePermitInfoDTO {

    public static final LargeCarnivorePermitInfoDTO create(@Nonnull final HarvestPermitApplication application,
                                                           @Nonnull final HarvestPermitApplicationSpeciesAmount applicationSpa,
                                                           final PermitDecision decision,
                                                           final HarvestPermit permit,
                                                           final HarvestPermitSpeciesAmount permitSpa,
                                                           final Integer harvests,
                                                           @Nonnull final LocalisedString rhy,
                                                           @Nonnull final LocalisedString rka,
                                                           final boolean onReindeerArea) {
        return new LargeCarnivorePermitInfoDTO(
                requireNonNull(application).getApplicationNumber(),
                F.mapNullable(permit, HarvestPermit::getPermitNumber),
                F.mapNullable(decision, PermitDecision::getDecisionType),
                F.mapNullable(decision, PermitDecision::getLockedDate),
                F.mapNullable(permitSpa, HarvestPermitSpeciesAmount::getBeginDate),
                F.mapNullable(permitSpa, HarvestPermitSpeciesAmount::getEndDate),
                F.mapNullable(permitSpa, HarvestPermitSpeciesAmount::getBeginDate2),
                F.mapNullable(permitSpa, HarvestPermitSpeciesAmount::getEndDate2),
                requireNonNull(applicationSpa).getSpecimenAmount(),
                F.mapNullable(permitSpa, HarvestPermitSpeciesAmount::getSpecimenAmount),
                harvests,
                requireNonNull(rhy),
                requireNonNull(rka),
                onReindeerArea);
    }

    private final Integer applicationNumber;
    private final String permitNumber;
    private final DecisionType decisionType;
    private final DateTime decisionTime;
    private final LocalDate beginDate;
    private final LocalDate endDate;
    private final LocalDate beginDate2;
    private final LocalDate endDate2;
    private final Float applied;
    private final Float granted;
    private final Integer harvests;
    private final LocalisedString rhy;
    private final LocalisedString rka;
    private final boolean onReindeerArea;

    private LargeCarnivorePermitInfoDTO(final Integer applicationNumber,
                                        final String permitNumber,
                                        final DecisionType decisionType,
                                        final DateTime decisionTime,
                                        final LocalDate beginDate,
                                        final LocalDate endDate,
                                        final LocalDate beginDate2,
                                        final LocalDate endDate2,
                                        final Float applied,
                                        final Float granted,
                                        final Integer harvests,
                                        final LocalisedString rhy,
                                        final LocalisedString rka,
                                        final boolean onReindeerArea) {
        this.applicationNumber = applicationNumber;
        this.permitNumber = permitNumber;
        this.decisionType = decisionType;
        this.decisionTime = decisionTime;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.beginDate2 = beginDate2;
        this.endDate2 = endDate2;
        this.applied = applied;
        this.granted = granted;
        this.harvests = harvests;
        this.rhy = rhy;
        this.rka = rka;
        this.onReindeerArea = onReindeerArea;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public DateTime getDecisionTime() {
        return decisionTime;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    public LocalDate getEndDate2() {
        return endDate2;
    }

    public Float getApplied() {
        return applied;
    }

    public Float getGranted() {
        return granted;
    }

    public Integer getHarvests() {
        return harvests;
    }

    public LocalisedString getRhy() {
        return rhy;
    }

    public LocalisedString getRka() {
        return rka;
    }

    public boolean isOnReindeerArea() {
        return onReindeerArea;
    }
}
