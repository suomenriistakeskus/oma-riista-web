package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClub_;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.MutableHuntingEndStatus;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
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
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

@Entity
@Access(AccessType.FIELD)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"club_id", "species_amount_id"})})
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

    @Valid
    @Embedded
    private ModeratedHarvestCounts moderatedHarvestCounts;

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

    // Hunting summary is considered empty if no summary data is present.
    public boolean isEmpty() {
        return huntingEndDate == null || areaSizeAndPopulation == null || areaSizeAndPopulation.isEmpty();
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
        // When moderator-override flag is true then all harvest count fields must be non-null and vice versa.
        return moderatorOverride == (moderatedHarvestCounts != null);
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
        this.moderatedHarvestCounts = new ModeratedHarvestCounts(harvestCounts);
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

        this.moderatedHarvestCounts = null;
        this.moderatorOverride = false;
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

    public ModeratedHarvestCounts getModeratedHarvestCounts() {
        return moderatedHarvestCounts;
    }

    public void setModeratedHarvestCounts(final ModeratedHarvestCounts moderatedHarvestCounts) {
        this.moderatedHarvestCounts = moderatedHarvestCounts;
    }
}
