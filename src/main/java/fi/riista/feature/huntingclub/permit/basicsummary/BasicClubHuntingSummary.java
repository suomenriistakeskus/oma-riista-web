package fi.riista.feature.huntingclub.permit.basicsummary;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClub_;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.summary.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfo;
import fi.riista.feature.huntingclub.permit.summary.MutableHuntingEndStatus;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

@Entity
@Access(AccessType.FIELD)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "club_id", "species_amount_id" }) })
public class BasicClubHuntingSummary extends LifecycleEntity<Long> implements MutableHuntingEndStatus {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClub club;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitSpeciesAmount speciesAmount;

    /**
     * Indicates whether this basic summary overrides all other information provided by club. If
     * true, harvest amounts for permit-club pair are derived from this entity instead of
     * calculating the amount of harvests associated to club and permit.
     */
    @Column(nullable = false)
    private boolean moderatorOverride;

    @Column(nullable = false)
    private boolean huntingFinished;

    @Column
    private LocalDate huntingEndDate;

    @Valid
    @Embedded
    private AreaSizeAndRemainingPopulation areaSizeAndPopulation;

    /**
     * Holds original hunting-finished status reported by the club itself in case of moderator-
     * override. Within revocation of moderator-override this date can be restored.
     */
    @Column
    private Boolean originalHuntingFinished;

    /**
     * Holds original hunting end date reported by the club itself in case of moderator-override.
     * Within revocation of moderator-override this date can be restored.
     */
    @Column
    private LocalDate originalHuntingEndDate;

    /**
     * Holds values reported originally by the club itself in case of moderator-override. Within
     * revocation of moderator-override these values can be restored.
     */
    @Valid
    @AttributeOverrides({
        @AttributeOverride(
                name = "totalHuntingArea",
                column = @Column(name = "original_total_hunting_area")),
        @AttributeOverride(
                name = "effectiveHuntingArea",
                column = @Column(name = "original_effective_hunting_area")),
        @AttributeOverride(
                name = "remainingPopulationInTotalArea",
                column = @Column(name = "original_remaining_population_in_total_area")),
        @AttributeOverride(
                name = "remainingPopulationInEffectiveArea",
                column = @Column(name = "original_remaining_population_in_effective_area"))
    })
    @Embedded
    private AreaSizeAndRemainingPopulation originalAreaSizeAndPopulation;

    // The following fields are only ever used when moderator fills data using mass insert dialog.

    @Min(0)
    @Column
    private Integer numberOfAdultMales;

    @Min(0)
    @Column
    private Integer numberOfAdultFemales;

    @Min(0)
    @Column
    private Integer numberOfYoungMales;

    @Min(0)
    @Column
    private Integer numberOfYoungFemales;

    @Min(0)
    @Column
    private Integer numberOfNonEdibleAdults;

    @Min(0)
    @Column
    private Integer numberOfNonEdibleYoungs;

    public BasicClubHuntingSummary() {
    }

    public BasicClubHuntingSummary(final HuntingClub club, final HarvestPermitSpeciesAmount speciesAmount) {
        setClub(club);
        setSpeciesAmount(speciesAmount);
    }

    @Override
    public Long getClubId() {
        return F.getId(club);
    }

    @Override
    public int getGameSpeciesCode() {
        return speciesAmount.getGameSpecies().getOfficialCode();
    }

    public ClubHuntingSummaryBasicInfo getBasicInfo() {
        final AreaSizeAndRemainingPopulation ap = getAreaSizeAndPopulation();

        return new ClubHuntingSummaryBasicInfo() {
            @Override
            public HuntingClub getClub() {
                return club;
            }

            @Override
            public int getGameSpeciesCode() {
                return BasicClubHuntingSummary.this.getGameSpeciesCode();
            }

            @Override
            public LocalDate getHuntingEndDate() {
                return huntingEndDate;
            }

            @Override
            public boolean isHuntingFinished() {
                return huntingFinished;
            }

            @Override
            public Integer getTotalHuntingArea() {
                return ap.getTotalHuntingArea();
            }

            // Returns either stored area size or value calculated from percentage of total area.
            @Override
            public Integer getEffectiveHuntingArea() {
                return ap.getEffectiveHuntingArea();
            }

            @Override
            public Float getEffectiveHuntingAreaPercentage() {
                return null;
            }

            @Override
            public Integer getRemainingPopulationInTotalArea() {
                return ap.getRemainingPopulationInTotalArea();
            }

            @Override
            public Integer getRemainingPopulationInEffectiveArea() {
                return ap.getRemainingPopulationInEffectiveArea();
            }
        };
    }

    public boolean isHuntingAreaAndRemainingPopulationPresent() {
        return getAreaSizeAndPopulation().isHuntingAreaAndRemainingPopulationPresent();
    }

    @AssertTrue
    public boolean isHuntingAreaAndRemainingPopulationPresentWhenHuntingFinished() {
        return !huntingFinished || isHuntingAreaAndRemainingPopulationPresent();
    }

    @AssertTrue
    public boolean isPresenceOfHarvestCountsConsistentWithModeratorOverride() {
        final Object[] harvestCounts = {
                numberOfAdultMales, numberOfAdultFemales, numberOfYoungMales, numberOfYoungFemales,
                numberOfNonEdibleAdults, numberOfNonEdibleYoungs
        };

        // When moderator-override flag is true then all harvest count fields must be non-null
        // and vice versa.
        return moderatorOverride ? F.allNotNull(harvestCounts) : F.allNull(harvestCounts);
    }

    @AssertTrue
    public boolean isHarvestCountsValid() {
        return Optional.ofNullable(getHarvestCounts())
                .map(HasHarvestCountsForPermit::isValid)
                .orElse(true);
    }

    public void doModeratorOverride(
            @Nonnull final LocalDate moderatedHuntingEndDate,
            @Nonnull final AreaSizeAndRemainingPopulation moderatedAreaSizeAndRemainingPopulation,
            @Nonnull final HasHarvestCountsForPermit harvestCounts) {

        Objects.requireNonNull(moderatedHuntingEndDate, "moderatedHuntingEndDate is null");
        Objects.requireNonNull(moderatedAreaSizeAndRemainingPopulation, "moderatedAreaSizeAndRemainingPopulation is null");
        Objects.requireNonNull(harvestCounts, "harvestCounts is null");

        if (!this.moderatorOverride) {
            this.originalHuntingFinished = this.huntingFinished;
            this.originalHuntingEndDate = this.huntingEndDate;
            this.originalAreaSizeAndPopulation = this.areaSizeAndPopulation;
        }

        this.huntingFinished = true;
        this.huntingEndDate = moderatedHuntingEndDate;
        this.areaSizeAndPopulation = moderatedAreaSizeAndRemainingPopulation;

        this.numberOfAdultMales = harvestCounts.getNumberOfAdultMales();
        this.numberOfAdultFemales = harvestCounts.getNumberOfAdultFemales();
        this.numberOfYoungMales = harvestCounts.getNumberOfYoungMales();
        this.numberOfYoungFemales = harvestCounts.getNumberOfYoungFemales();
        this.numberOfNonEdibleAdults = harvestCounts.getNumberOfNonEdibleAdults();
        this.numberOfNonEdibleYoungs = harvestCounts.getNumberOfNonEdibleYoungs();

        this.moderatorOverride = true;
    }

    public void revokeModeratorOverride() {
        Preconditions.checkState(moderatorOverride, "Cannot revoke non-moderator-overridden state");

        this.huntingFinished = Optional.ofNullable(this.originalHuntingFinished).orElse(false);
        this.originalHuntingFinished = null;

        this.huntingEndDate = this.originalHuntingEndDate;
        this.originalHuntingEndDate = null;

        this.areaSizeAndPopulation = this.originalAreaSizeAndPopulation;
        this.originalAreaSizeAndPopulation = null;

        this.numberOfAdultMales = null;
        this.numberOfAdultFemales = null;
        this.numberOfYoungMales = null;
        this.numberOfYoungFemales = null;
        this.numberOfNonEdibleAdults = null;
        this.numberOfNonEdibleYoungs = null;

        this.moderatorOverride = false;
    }

    @Nullable
    public HasHarvestCountsForPermit getHarvestCounts() {
        return moderatorOverride
                ? HasHarvestCountsForPermit.of(numberOfAdultMales, numberOfAdultFemales, numberOfYoungMales,
                        numberOfYoungFemales, numberOfNonEdibleAdults, numberOfNonEdibleYoungs)
                : null;
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hunting_summary_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingClub getClub() {
        return club;
    }

    public void setClub(final HuntingClub club) {
        CriteriaUtils.updateInverseCollection(HuntingClub_.basicHuntingSummaries, this, this.club, club);
        this.club = club;
    }

    public HarvestPermitSpeciesAmount getSpeciesAmount() {
        return speciesAmount;
    }

    public void setSpeciesAmount(final HarvestPermitSpeciesAmount speciesAmount) {
        this.speciesAmount = speciesAmount;
    }

    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    @Override
    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    @Override
    public void setHuntingFinished(boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    @Override
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    @Override
    public void setHuntingEndDate(final LocalDate huntingEndDate) {
        this.huntingEndDate = huntingEndDate;
    }

    public AreaSizeAndRemainingPopulation getAreaSizeAndPopulation() {
        if (areaSizeAndPopulation == null) {
            areaSizeAndPopulation = new AreaSizeAndRemainingPopulation();
        }
        return areaSizeAndPopulation;
    }

    public void setAreaSizeAndPopulation(final AreaSizeAndRemainingPopulation areaSizeAndPopulation) {
        this.areaSizeAndPopulation = areaSizeAndPopulation;
    }

    public Integer getNumberOfAdultMales() {
        return numberOfAdultMales;
    }

    public Integer getNumberOfAdultFemales() {
        return numberOfAdultFemales;
    }

    public Integer getNumberOfYoungMales() {
        return numberOfYoungMales;
    }

    public Integer getNumberOfYoungFemales() {
        return numberOfYoungFemales;
    }

    public Integer getNumberOfNonEdibleAdults() {
        return numberOfNonEdibleAdults;
    }

    public Integer getNumberOfNonEdibleYoungs() {
        return numberOfNonEdibleYoungs;
    }

}
