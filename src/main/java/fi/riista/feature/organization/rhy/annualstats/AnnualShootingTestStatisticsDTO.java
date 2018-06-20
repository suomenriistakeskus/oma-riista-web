package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.util.NumberUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.util.Optional;

public class AnnualShootingTestStatisticsDTO {

    public static AnnualShootingTestStatisticsDTO create(@Nonnull final AnnualShootingTestStatistics entity) {
        final AnnualShootingTestStatisticsDTO dto = new AnnualShootingTestStatisticsDTO();

        if (entity.isFirearmTestEventsManuallyOverridden()) {
            dto.setModeratorOverriddenFirearmTestEvents(entity.getFirearmTestEvents());
        } else {
            dto.setFirearmTestEvents(entity.getFirearmTestEvents());
        }

        if (entity.isBowTestEventsManuallyOverridden()) {
            dto.setModeratorOverriddenBowTestEvents(entity.getBowTestEvents());
        } else {
            dto.setBowTestEvents(entity.getBowTestEvents());
        }

        dto.setAllMooseAttempts(entity.getAllMooseAttempts());
        dto.setQualifiedMooseAttempts(entity.getQualifiedMooseAttempts());
        dto.setAllBearAttempts(entity.getAllBearAttempts());
        dto.setQualifiedBearAttempts(entity.getQualifiedBearAttempts());
        dto.setAllRoeDeerAttempts(entity.getAllRoeDeerAttempts());
        dto.setQualifiedRoeDeerAttempts(entity.getQualifiedRoeDeerAttempts());
        dto.setAllBowAttempts(entity.getAllBowAttempts());
        dto.setQualifiedBowAttempts(entity.getQualifiedBowAttempts());

        dto.setShootingTestOfficials(entity.getShootingTestOfficials());

        dto.setLastModified(entity.getLastModified());

        return dto;
    }

    // Rihlatun luotiaseen kokeet, kpl
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer firearmTestEvents;

    // Rihlatun luotiaseen kokeet, kpl, moderaattorin ylimäärittelemä
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer moderatorOverriddenFirearmTestEvents;

    // Jousikokeet, kpl
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer bowTestEvents;

    // Jousikokeet, kpl, moderaattorin ylimäärittelemä
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer moderatorOverriddenBowTestEvents;

    // Ampumakoesuoritusyrityksiä, hirvi ja peura, kpl
    @Min(0)
    private Integer allMooseAttempts;

    // Ampumakoesuoritusyrityksiä, hirvi ja peura, hyväksytyt, kpl
    @Min(0)
    private Integer qualifiedMooseAttempts;

    // Ampumakoesuoritusyrityksiä, karhu, kpl
    @Min(0)
    private Integer allBearAttempts;

    // Ampumakoesuoritusyrityksiä, karhu, hyväksytyt, kpl
    @Min(0)
    private Integer qualifiedBearAttempts;

    // Ampumakoesuoritusyrityksiä, metsäkauris, kpl
    @Min(0)
    private Integer allRoeDeerAttempts;

    // Ampumakoesuoritusyrityksiä, metsäkauris, hyväksytyt, kpl
    @Min(0)
    private Integer qualifiedRoeDeerAttempts;

    // Ampumakoesuoritusyrityksiä, jousi, kpl
    @Min(0)
    private Integer allBowAttempts;

    // Ampumakoesuoritusyrityksiä, jousi, hyväksytyt, kpl
    @Min(0)
    private Integer qualifiedBowAttempts;

    // Ampumakokeen vastaanottajien lukumäärä
    @Min(0)
    private Integer shootingTestOfficials;

    // Updated when any of the manually updateable fields is changed.
    private DateTime lastModified;

    @AssertTrue
    public boolean isValidInTermsOfEventCountDefinition() {
        return (firearmTestEvents == null || moderatorOverriddenFirearmTestEvents == null)
                && (bowTestEvents == null || moderatorOverriddenBowTestEvents == null);
    }

    @JsonGetter(value = "allShootingTestEvents")
    public int countAllShootingTestEvents() {
        final int firearmTestEvents = Optional
                .ofNullable(getFirearmTestEvents())
                .orElseGet(() -> NumberUtils.getIntValueOrZero(getModeratorOverriddenFirearmTestEvents()));

        final int bowTestEvents = Optional
                .ofNullable(getBowTestEvents())
                .orElseGet(() -> NumberUtils.getIntValueOrZero(getModeratorOverriddenBowTestEvents()));

        return firearmTestEvents + bowTestEvents;
    }

    // Accessors -->

    public Integer getFirearmTestEvents() {
        return firearmTestEvents;
    }

    public void setFirearmTestEvents(final Integer firearmTestEvents) {
        this.firearmTestEvents = firearmTestEvents;
    }

    public Integer getModeratorOverriddenFirearmTestEvents() {
        return moderatorOverriddenFirearmTestEvents;
    }

    public void setModeratorOverriddenFirearmTestEvents(final Integer moderatorOverriddenFirearmTestEvents) {
        this.moderatorOverriddenFirearmTestEvents = moderatorOverriddenFirearmTestEvents;
    }

    public Integer getBowTestEvents() {
        return bowTestEvents;
    }

    public void setBowTestEvents(final Integer bowTestEvents) {
        this.bowTestEvents = bowTestEvents;
    }

    public Integer getModeratorOverriddenBowTestEvents() {
        return moderatorOverriddenBowTestEvents;
    }

    public void setModeratorOverriddenBowTestEvents(final Integer moderatorOverriddenBowTestEvents) {
        this.moderatorOverriddenBowTestEvents = moderatorOverriddenBowTestEvents;
    }

    public Integer getAllMooseAttempts() {
        return allMooseAttempts;
    }

    public void setAllMooseAttempts(final Integer allMooseAttempts) {
        this.allMooseAttempts = allMooseAttempts;
    }

    public Integer getQualifiedMooseAttempts() {
        return qualifiedMooseAttempts;
    }

    public void setQualifiedMooseAttempts(final Integer qualifiedMooseAttempts) {
        this.qualifiedMooseAttempts = qualifiedMooseAttempts;
    }

    public Integer getAllBearAttempts() {
        return allBearAttempts;
    }

    public void setAllBearAttempts(final Integer allBearAttempts) {
        this.allBearAttempts = allBearAttempts;
    }

    public Integer getQualifiedBearAttempts() {
        return qualifiedBearAttempts;
    }

    public void setQualifiedBearAttempts(final Integer qualifiedBearAttempts) {
        this.qualifiedBearAttempts = qualifiedBearAttempts;
    }

    public Integer getAllRoeDeerAttempts() {
        return allRoeDeerAttempts;
    }

    public void setAllRoeDeerAttempts(final Integer allRoeDeerAttempts) {
        this.allRoeDeerAttempts = allRoeDeerAttempts;
    }

    public Integer getQualifiedRoeDeerAttempts() {
        return qualifiedRoeDeerAttempts;
    }

    public void setQualifiedRoeDeerAttempts(final Integer qualifiedRoeDeerAttempts) {
        this.qualifiedRoeDeerAttempts = qualifiedRoeDeerAttempts;
    }

    public Integer getAllBowAttempts() {
        return allBowAttempts;
    }

    public void setAllBowAttempts(final Integer allBowAttempts) {
        this.allBowAttempts = allBowAttempts;
    }

    public Integer getQualifiedBowAttempts() {
        return qualifiedBowAttempts;
    }

    public void setQualifiedBowAttempts(final Integer qualifiedBowAttempts) {
        this.qualifiedBowAttempts = qualifiedBowAttempts;
    }

    public Integer getShootingTestOfficials() {
        return shootingTestOfficials;
    }

    public void setShootingTestOfficials(final Integer shootingTestOfficials) {
        this.shootingTestOfficials = shootingTestOfficials;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
