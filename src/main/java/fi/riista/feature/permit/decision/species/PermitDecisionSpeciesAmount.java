package fi.riista.feature.permit.decision.species;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.decision.PermitDecision;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionSpeciesAmount extends LifecycleEntity<Long> implements Has2BeginEndDates {

    public static LocalDate getDefaultMooselikeBeginDate(final int huntingYear) {
        return new LocalDate(huntingYear, 9, 1);
    }

    public static LocalDate getDefaultMooselikeEndDate(final int huntingYear) {
        return new LocalDate(huntingYear + 1, 1, 15);
    }

    public enum RestrictionType {
        /**
         * Aikuisia enintään
         */
        AE,

        /**
         * Aikuisia uroksia enintään
         */
        AU;

        public static PermitDecisionSpeciesAmount.RestrictionType ofNullable(String str) {
            return str == null ? null : valueOf(str);
        }
    }

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecision permitDecision;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GameSpecies gameSpecies;

    @Column(nullable = false)
    private float amount;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private LocalDate beginDate2;

    @Column
    private LocalDate endDate2;

    @Enumerated(EnumType.STRING)
    @Column
    private RestrictionType restrictionType;

    @Column
    private Float restrictionAmount;

    @Column(nullable = false)
    private boolean amountComplete;

    @Column(nullable = false)
    private boolean forbiddenMethodComplete;

    public PermitDecisionSpeciesAmount() {
    }

    public PermitDecisionSpeciesAmount(final PermitDecision permitDecision,
                                       final GameSpecies gameSpecies,
                                       final float amount,
                                       final RestrictionType restrictionType,
                                       final Float restrictionAmount,
                                       final LocalDate beginDate,
                                       final LocalDate endDate) {
        this.permitDecision = permitDecision;
        this.gameSpecies = gameSpecies;
        this.amount = amount;
        this.restrictionType = restrictionType;
        this.restrictionAmount = restrictionAmount;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    // Helpers -->

    public int getPermitYear() {
        return beginDate.getYear();
    }

    public boolean hasGrantedSpecies() {
        return amount > 0;
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_species_amount_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(final PermitDecision decision) {
        this.permitDecision = decision;
    }

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public Float getRestrictionAmount() {
        return restrictionAmount;
    }

    public void setRestrictionAmount(Float restrictionAmount) {
        this.restrictionAmount = restrictionAmount;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @Override
    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    @Override
    public void setBeginDate2(final LocalDate beginDate2) {
        this.beginDate2 = beginDate2;
    }

    @Override
    public LocalDate getEndDate2() {
        return endDate2;
    }

    @Override
    public void setEndDate2(final LocalDate endDate2) {
        this.endDate2 = endDate2;
    }

    public boolean isAmountComplete() {
        return amountComplete;
    }

    public void setAmountComplete(final boolean amountComplete) {
        this.amountComplete = amountComplete;
    }

    public boolean isForbiddenMethodComplete() {
        return forbiddenMethodComplete;
    }

    public void setForbiddenMethodComplete(final boolean forbiddenMethodComplete) {
        this.forbiddenMethodComplete = forbiddenMethodComplete;
    }
}
