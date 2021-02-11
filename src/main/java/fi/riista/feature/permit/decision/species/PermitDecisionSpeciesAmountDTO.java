package fi.riista.feature.permit.decision.species;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class PermitDecisionSpeciesAmountDTO implements Has2BeginEndDates, HasID<Long> {
    public static final int MAX_SPECIES_AMOUNT_VALUE = 100_000;
    public static final int MIN_SPECIES_AMOUNT_VALUE = 0;

    @Nonnull
    public static PermitDecisionSpeciesAmountDTO create(@Nonnull final PermitDecisionSpeciesAmount speciesAmount,
                                                        @Nonnull final HarvestPermitApplication application,
                                                        @Nonnull final HarvestPermitApplicationSpeciesAmount applicationAmount) {
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
            @Nonnull final HarvestPermitApplicationSpeciesAmount applicationAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(species, "species must not be null");
        requireNonNull(application);
        requireNonNull(applicationAmount, "applicationAmount must not be null");

        copyDatesFrom(speciesAmount);

        this.id = speciesAmount.getId();
        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.specimenAmount = speciesAmount.getSpecimenAmount();
        this.nestAmount = speciesAmount.getNestAmount();
        this.eggAmount = speciesAmount.getEggAmount();
        this.constructionAmount = speciesAmount.getConstructionAmount();
        this.restrictionType = speciesAmount.getRestrictionType();
        this.restrictionAmount = speciesAmount.getRestrictionAmount();
        this.applicationSpecimenAmount = applicationAmount.getSpecimenAmount();
        this.applicationNestAmount = applicationAmount.getNestAmount();
        this.applicationEggAmount = applicationAmount.getEggAmount();
        this.applicationConstructionAmount = applicationAmount.getConstructionAmount();
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
    private Float specimenAmount;
    private Integer nestAmount;
    private Integer eggAmount;
    private Integer constructionAmount;
    private Float applicationSpecimenAmount;
    private Integer applicationNestAmount;
    private Integer applicationEggAmount;
    private Integer applicationConstructionAmount;
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
    public boolean isPresentAmountsValid() {
        return specimenAmount != null
                || Stream.of(nestAmount, eggAmount, constructionAmount)
                .filter(Objects::nonNull)
                .allMatch(number -> number.compareTo(MIN_SPECIES_AMOUNT_VALUE) >= 0 && number.compareTo(MAX_SPECIES_AMOUNT_VALUE) <= 0);
    }

    @AssertTrue
    public boolean isSomeAmountPresent() {
        return Stream.of(specimenAmount, nestAmount, eggAmount, constructionAmount)
                .filter(Objects::nonNull)
                .map(anyAmount->true)
                .findAny()
                .orElse(false);
    }

    @AssertTrue
    public boolean isRestrictionAmountValid() {
        return restrictionAmount == null || specimenAmount > 0 && restrictionAmount > 0 && restrictionAmount <= specimenAmount;
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

    public Float getSpecimenAmount() {
        return specimenAmount;
    }

    public void setSpecimenAmount(final Float specimenAmount) {
        this.specimenAmount = specimenAmount;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public void setNestAmount(final Integer nestAmount) {
        this.nestAmount = nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }

    public void setConstructionAmount(final Integer constructionAmount) {
        this.constructionAmount = constructionAmount;
    }

    public Float getApplicationSpecimenAmount() {
        return applicationSpecimenAmount;
    }

    public void setApplicationSpecimenAmount(final Float applicationSpecimenAmount) {
        this.applicationSpecimenAmount = applicationSpecimenAmount;
    }

    public Integer getApplicationNestAmount() {
        return applicationNestAmount;
    }

    public void setApplicationNestAmount(final Integer applicationNestAmount) {
        this.applicationNestAmount = applicationNestAmount;
    }

    public Integer getApplicationEggAmount() {
        return applicationEggAmount;
    }

    public void setApplicationEggAmount(final Integer applicationEggAmount) {
        this.applicationEggAmount = applicationEggAmount;
    }

    public Integer getApplicationConstructionAmount() {
        return applicationConstructionAmount;
    }

    public void setApplicationConstructionAmount(final Integer applicationConstructionAmount) {
        this.applicationConstructionAmount = applicationConstructionAmount;
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
