package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.util.Objects;
import java.util.Optional;

public class BasicClubHuntingSummaryDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static BasicClubHuntingSummaryDTO transform(@Nonnull final BasicClubHuntingSummary summary,
                                                       final int permitAreaSize) {
        final BasicClubHuntingSummaryDTO dto = new BasicClubHuntingSummaryDTO();
        DtoUtil.copyBaseFields(summary, dto);

        final HarvestPermitSpeciesAmount speciesAmount = summary.getSpeciesAmount();
        final HarvestPermit harvestPermit = speciesAmount.getHarvestPermit();
        dto.setHarvestPermitId(harvestPermit.getId());
        dto.setPermitAreaSize(permitAreaSize);
        dto.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());

        final HuntingClub club = summary.getClub();
        dto.setClubId(club.getId());
        dto.setNameFI(club.getNameFinnish());
        dto.setNameSV(club.getNameSwedish());

        dto.setModeratorOverridden(summary.isModeratorOverride());
        dto.setHuntingFinished(summary.isHuntingFinished());
        dto.setHuntingEndDate(summary.getHuntingEndDate());

        Optional.ofNullable(summary.getAreaSizeAndPopulation()).ifPresent(dto::withAreaSizeAndRemainingPopulation);
        Optional.ofNullable(summary.getModeratedHarvestCounts()).ifPresent(dto::setHarvestCounts);

        return dto;
    }

    private Long id;
    private Integer rev;

    private long harvestPermitId;
    private int permitAreaSize;
    private int gameSpeciesCode;

    private long clubId;

    // Localised club names

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    private boolean moderatorOverridden;

    private LocalDate huntingEndDate;

    private boolean huntingFinished;

    @Min(0)
    private Integer totalHuntingArea;

    @Min(0)
    private Integer effectiveHuntingArea;

    @Min(0)
    private Integer remainingPopulationInTotalArea;

    @Min(0)
    private Integer remainingPopulationInEffectiveArea;

    @Min(0)
    @JsonProperty(value = "adultMales")
    private Integer numberOfAdultMales;

    @Min(0)
    @JsonProperty(value = "adultFemales")
    private Integer numberOfAdultFemales;

    @Min(0)
    @JsonProperty(value = "youngMales")
    private Integer numberOfYoungMales;

    @Min(0)
    @JsonProperty(value = "youngFemales")
    private Integer numberOfYoungFemales;

    @Min(0)
    @JsonProperty(value = "adultsNotEdible")
    private Integer numberOfNonEdibleAdults;

    @Min(0)
    @JsonProperty(value = "youngsNotEdible")
    private Integer numberOfNonEdibleYoungs;

    @JsonIgnore
    private boolean createdWithinModeration;

    public AreaSizeAndRemainingPopulation getAreaSizeAndRemainingPopulation() {
        return new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(totalHuntingArea)
                .withEffectiveHuntingArea(effectiveHuntingArea)
                .withRemainingPopulationInTotalArea(remainingPopulationInTotalArea)
                .withRemainingPopulationInEffectiveArea(remainingPopulationInEffectiveArea);
    }

    public BasicClubHuntingSummaryDTO withAreaSizeAndRemainingPopulation(final AreaSizeAndRemainingPopulation that) {
        setTotalHuntingArea(that.getTotalHuntingArea());
        setEffectiveHuntingArea(that.getEffectiveHuntingArea());
        setRemainingPopulationInTotalArea(that.getRemainingPopulationInTotalArea());
        setRemainingPopulationInEffectiveArea(that.getRemainingPopulationInEffectiveArea());
        return this;
    }

    public void setHarvestCounts(final @Nonnull HasHarvestCountsForPermit harvestCounts) {
        Objects.requireNonNull(harvestCounts);
        this.numberOfAdultMales = harvestCounts.getNumberOfAdultMales();
        this.numberOfAdultFemales = harvestCounts.getNumberOfAdultFemales();
        this.numberOfYoungMales = harvestCounts.getNumberOfYoungMales();
        this.numberOfYoungFemales = harvestCounts.getNumberOfYoungFemales();
        this.numberOfNonEdibleAdults = harvestCounts.getNumberOfNonEdibleAdults();
        this.numberOfNonEdibleYoungs = harvestCounts.getNumberOfNonEdibleYoungs();
    }

    public HasHarvestCountsForPermit getHarvestCounts() {
        return HasHarvestCountsForPermit.of(numberOfAdultMales, numberOfAdultFemales, numberOfYoungMales,
                numberOfYoungFemales, numberOfNonEdibleAdults, numberOfNonEdibleYoungs);
    }

    @AssertTrue
    public boolean isModerationStateValid() {
        return !isModeratorOverridden() || createdWithinModeration;
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public long getHarvestPermitId() {
        return harvestPermitId;
    }

    public void setHarvestPermitId(final long harvestPermitId) {
        this.harvestPermitId = harvestPermitId;
    }

    public int getPermitAreaSize() {
        return permitAreaSize;
    }

    public void setPermitAreaSize(int permitAreaSize) {
        this.permitAreaSize = permitAreaSize;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public long getClubId() {
        return clubId;
    }

    public void setClubId(final long clubId) {
        this.clubId = clubId;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(final String nameFi) {
        this.nameFI = nameFi;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(final String nameSv) {
        this.nameSV = nameSv;
    }

    public boolean isModeratorOverridden() {
        return moderatorOverridden;
    }

    public void setModeratorOverridden(final boolean overriddenByModerator) {
        this.moderatorOverridden = overriddenByModerator;
    }

    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    public void setHuntingEndDate(final LocalDate huntingEndDate) {
        this.huntingEndDate = huntingEndDate;
    }

    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    public void setHuntingFinished(boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public void setTotalHuntingArea(final Integer totalHuntingArea) {
        this.totalHuntingArea = totalHuntingArea;
    }

    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public void setEffectiveHuntingArea(final Integer effectiveHuntingArea) {
        this.effectiveHuntingArea = effectiveHuntingArea;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public void setRemainingPopulationInTotalArea(final Integer remainingPopulationInTotalArea) {
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    public void setRemainingPopulationInEffectiveArea(final Integer remainingPopulationInEffectiveArea) {
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }

    public Integer getNumberOfAdultMales() {
        return numberOfAdultMales;
    }

    public void setNumberOfAdultMales(final Integer numberOfAdultMales) {
        this.numberOfAdultMales = numberOfAdultMales;
    }

    public Integer getNumberOfAdultFemales() {
        return numberOfAdultFemales;
    }

    public void setNumberOfAdultFemales(final Integer numberOfAdultFemales) {
        this.numberOfAdultFemales = numberOfAdultFemales;
    }

    public Integer getNumberOfYoungMales() {
        return numberOfYoungMales;
    }

    public void setNumberOfYoungMales(final Integer numberOfYoungMales) {
        this.numberOfYoungMales = numberOfYoungMales;
    }

    public Integer getNumberOfYoungFemales() {
        return numberOfYoungFemales;
    }

    public void setNumberOfYoungFemales(final Integer numberOfYoungFemales) {
        this.numberOfYoungFemales = numberOfYoungFemales;
    }

    public Integer getNumberOfNonEdibleAdults() {
        return numberOfNonEdibleAdults;
    }

    public void setNumberOfNonEdibleAdults(final Integer numberOfNonEdibleAdults) {
        this.numberOfNonEdibleAdults = numberOfNonEdibleAdults;
    }

    public Integer getNumberOfNonEdibleYoungs() {
        return numberOfNonEdibleYoungs;
    }

    public void setNumberOfNonEdibleYoungs(final Integer numberOfNonEdibleYoungs) {
        this.numberOfNonEdibleYoungs = numberOfNonEdibleYoungs;
    }
}
