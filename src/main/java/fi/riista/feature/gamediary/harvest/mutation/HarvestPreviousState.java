package fi.riista.feature.gamediary.harvest.mutation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestPreviousState {
    private final Person previousAuthor;
    private final GeoLocation previousLocation;
    private final GameSpecies previousGameSpecies;
    private final HarvestPermit previousHarvestPermit;
    private final HarvestSeason previousHarvestSeason;
    private final HarvestQuota previousHarvestQuota;
    private final HarvestReportState previousHarvestReportState;
    private final HarvestReportingType previousReportingType;

    public HarvestPreviousState(@Nonnull final Harvest harvestBeforeUpdate) {
        this.previousReportingType = harvestBeforeUpdate.resolveReportingType();
        this.previousHarvestReportState = harvestBeforeUpdate.getHarvestReportState();
        this.previousAuthor = harvestBeforeUpdate.getAuthor();
        this.previousLocation = harvestBeforeUpdate.getGeoLocation();
        this.previousGameSpecies = harvestBeforeUpdate.getSpecies();
        this.previousHarvestPermit = harvestBeforeUpdate.getHarvestPermit();
        this.previousHarvestSeason = harvestBeforeUpdate.getHarvestSeason();
        this.previousHarvestQuota = harvestBeforeUpdate.getHarvestQuota();
    }

    public HarvestReportingType getPreviousReportingType() {
        return previousReportingType;
    }

    public GeoLocation getPreviousLocation() {
        return previousLocation;
    }

    public Person getPreviousAuthor() {
        return previousAuthor;
    }

    public boolean isHarvestReportDone() {
        return previousHarvestReportState != null;
    }

    public boolean hasHarvestReportStateChanged(final Harvest harvest) {
        return previousHarvestReportState != harvest.getHarvestReportState();
    }

    public boolean shouldInitReportState(final Harvest harvest) {
        return hasSpeciesChanged(harvest)
                || hasPermitChanged(harvest)
                || hasSeasonChanged(harvest)
                || hasQuotaChanged(harvest)
                || hasReportingTypeChanged(harvest);
    }

    boolean hasReportingTypeChanged(final Harvest harvest) {
        return previousReportingType != harvest.resolveReportingType();
    }

    boolean hasSeasonChanged(final Harvest harvest) {
        return !Objects.equals(harvest.getHarvestSeason(), previousHarvestSeason);
    }

    boolean hasQuotaChanged(final Harvest harvest) {
        return !Objects.equals(harvest.getHarvestQuota(), previousHarvestQuota);
    }

    boolean hasSpeciesChanged(final Harvest harvest) {
        return !Objects.equals(harvest.getSpecies(), previousGameSpecies);
    }

    boolean hasPermitChanged(final Harvest harvest) {
        return !Objects.equals(harvest.getHarvestPermit(), previousHarvestPermit);
    }
}
