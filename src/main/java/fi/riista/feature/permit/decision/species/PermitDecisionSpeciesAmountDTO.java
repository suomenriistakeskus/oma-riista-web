package fi.riista.feature.permit.decision.species;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class PermitDecisionSpeciesAmountDTO implements Has2BeginEndDates, HasID<Long> {
    private static final int MAX_SPECIES_AMOUNT = 100_000;
    private static final int MIN_SPECIES_AMOUNT = 0;

    @Nonnull
    public static PermitDecisionSpeciesAmountDTO create(@Nonnull final PermitDecisionSpeciesAmount speciesAmount,
                                                        @Nonnull final HarvestPermitApplication application,
                                                        final float applicationAmount) {
        return new PermitDecisionSpeciesAmountDTO(
                speciesAmount,
                speciesAmount.getGameSpecies(),
                application,
                applicationAmount);
    }

    public PermitDecisionSpeciesAmountDTO() {
    }

    public PermitDecisionSpeciesAmountDTO(
            @Nonnull final PermitDecisionSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species,
            @Nonnull final HarvestPermitApplication application,
            final float applicationAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(species, "species must not be null");
        requireNonNull(application);

        copyDatesFrom(speciesAmount);

        this.id = speciesAmount.getId();
        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.amount = speciesAmount.getAmount();
        this.restrictionType = speciesAmount.getRestrictionType();
        this.restrictionAmount = speciesAmount.getRestrictionAmount();
        this.applicationAmount = applicationAmount;
        this.amountComplete = speciesAmount.isAmountComplete();
        this.forbiddenMethodComplete = speciesAmount.isForbiddenMethodComplete();

        final PermitDecisionSpeciesAmountDateRestriction restriction =
                PermitDecisionSpeciesAmountDateRestriction.create(speciesAmount);

        this.minBeginDate = restriction.resolveMinBeginDate();
        this.maxBeginDate = restriction.resolveMaxBeginDate();
    }

    @NotNull
    private Long id;
    private int gameSpeciesCode;
    private float amount;
    private float applicationAmount;
    private PermitDecisionSpeciesAmount.RestrictionType restrictionType;
    private Float restrictionAmount;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    private LocalDate beginDate2;

    private LocalDate endDate2;

    private LocalDate minBeginDate;
    private LocalDate maxBeginDate;

    private boolean amountComplete;
    private boolean forbiddenMethodComplete;

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    @AssertTrue
    public boolean isRestrictionAmountValid() {
        return restrictionAmount == null || amount > 0 && restrictionAmount > 0 && restrictionAmount <= amount;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getApplicationAmount() {
        return applicationAmount;
    }

    public void setApplicationAmount(final float applicationAmount) {
        this.applicationAmount = applicationAmount;
    }

    public PermitDecisionSpeciesAmount.RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(PermitDecisionSpeciesAmount.RestrictionType restrictionType) {
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

    public LocalDate getMinBeginDate() {
        return minBeginDate;
    }

    public void setMinBeginDate(LocalDate minBeginDate) {
        this.minBeginDate = minBeginDate;
    }

    public LocalDate getMaxBeginDate() {
        return maxBeginDate;
    }

    public void setMaxBeginDate(LocalDate maxBeginDate) {
        this.maxBeginDate = maxBeginDate;
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
