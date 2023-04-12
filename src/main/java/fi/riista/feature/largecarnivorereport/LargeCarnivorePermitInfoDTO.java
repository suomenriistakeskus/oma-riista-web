package fi.riista.feature.largecarnivorereport;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecision.DecisionType;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class LargeCarnivorePermitInfoDTO {

    public static final LargeCarnivorePermitInfoDTO create(@Nonnull final HarvestPermitApplication application,
                                                           @Nonnull final HarvestPermitApplicationSpeciesAmount applicationSpa,
                                                           final PermitDecision decision,
                                                           final HarvestPermit permit,
                                                           final PermitDecisionSpeciesAmount decisionSpa,
                                                           final Integer harvests,
                                                           @Nonnull final LocalisedString rhy,
                                                           @Nonnull final LocalisedString rka,
                                                           final boolean onReindeerArea) {
        return new LargeCarnivorePermitInfoDTO(
                requireNonNull(application).getApplicationNumber(),
                F.mapNullable(permit, HarvestPermit::getPermitNumber),
                F.mapNullable(decision, PermitDecision::getDecisionType),
                F.mapNullable(decision, PermitDecision::getLockedDate),
                F.mapNullable(decisionSpa, PermitDecisionSpeciesAmount::getBeginDate),
                F.mapNullable(decisionSpa, PermitDecisionSpeciesAmount::getEndDate),
                F.mapNullable(decisionSpa, PermitDecisionSpeciesAmount::getBeginDate2),
                F.mapNullable(decisionSpa, PermitDecisionSpeciesAmount::getEndDate2),
                requireNonNull(applicationSpa).getSpecimenAmount(),
                F.mapNullable(decisionSpa, PermitDecisionSpeciesAmount::getSpecimenAmount),
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

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof LargeCarnivorePermitInfoDTO)) {
            return false;
        } else {
            final LargeCarnivorePermitInfoDTO that = (LargeCarnivorePermitInfoDTO) o;

            return Objects.equals(this.applicationNumber, that.applicationNumber)
                    && Objects.equals(this.permitNumber, that.permitNumber)
                    && Objects.equals(this.decisionType, that.decisionType)
                    && Objects.equals(this.decisionTime, that.decisionTime)
                    && Objects.equals(this.beginDate, that.beginDate)
                    && Objects.equals(this.endDate, that.endDate)
                    && Objects.equals(this.beginDate2, that.beginDate2)
                    && Objects.equals(this.endDate2, that.endDate2)
                    && Objects.equals(this.applied, that.applied)
                    && Objects.equals(this.granted, that.granted)
                    && Objects.equals(this.harvests, that.harvests)
                    && Objects.equals(this.rhy, that.rhy)
                    && Objects.equals(this.rka, that.rka)
                    && Objects.equals(this.onReindeerArea, that.onReindeerArea);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationNumber, permitNumber, decisionType, decisionTime, beginDate, endDate, beginDate2,
                endDate2, applied, granted, harvests, rhy, rka, onReindeerArea);
    }
}
