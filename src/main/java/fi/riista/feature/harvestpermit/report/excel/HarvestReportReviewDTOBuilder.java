package fi.riista.feature.harvestpermit.report.excel;

import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

public class HarvestReportReviewDTOBuilder {
    private long id;
    private LocalDateTime pointOfTime;
    private LocalDateTime creationTime;
    private LocalDateTime harvestReportDate;
    private LocalisedString species;
    private LocalisedString rkaName;
    private LocalisedString rhyName;
    private String authorHunterNumber;
    private String shooterHunterNumber;
    private String permitType;
    private String permitNumber;
    private String partnerOfficialCode;
    private LocalisedString partner;

    private boolean createdByModerator;
    public static HarvestReportReviewDTOBuilder builder() {
        return new HarvestReportReviewDTOBuilder();
    }

    public HarvestReportReviewDTOBuilder setId(final long id) {
        this.id = id;
        return this;
    }

    public HarvestReportReviewDTOBuilder setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
        return this;
    }

    public HarvestReportReviewDTOBuilder setCreationTime(final LocalDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public HarvestReportReviewDTOBuilder setHarvestReportDate(final LocalDateTime harvestReportDate) {
        this.harvestReportDate = harvestReportDate;
        return this;
    }

    public HarvestReportReviewDTOBuilder setSpecies(final LocalisedString species) {
        this.species = species;
        return this;
    }

    public HarvestReportReviewDTOBuilder setRkaName(final LocalisedString rkaName) {
        this.rkaName = rkaName;
        return this;
    }

    public HarvestReportReviewDTOBuilder setRhyName(final LocalisedString rhyName) {
        this.rhyName = rhyName;
        return this;
    }

    public HarvestReportReviewDTOBuilder setAuthorHunterNumber(final String authorHunterNumber) {
        this.authorHunterNumber = authorHunterNumber;
        return this;
    }

    public HarvestReportReviewDTOBuilder setShooterHunterNumber(final String shooterHunterNumber) {
        this.shooterHunterNumber = shooterHunterNumber;
        return this;
    }

    public HarvestReportReviewDTOBuilder setPermitType(final String permitType) {
        this.permitType = permitType;
        return this;
    }

    public HarvestReportReviewDTOBuilder setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
        return this;
    }

    public HarvestReportReviewDTOBuilder setPartnerOfficialCode(final String partnerOfficialCode) {
        this.partnerOfficialCode = partnerOfficialCode;
        return this;
    }

    public HarvestReportReviewDTOBuilder setPartner(final LocalisedString partner) {
        this.partner = partner;
        return this;
    }

    public HarvestReportReviewDTOBuilder setCreatedByModerator(final boolean createdByModerator) {
        this.createdByModerator = createdByModerator;
        return this;
    }

    public HarvestReportReviewDTO build() {
        return new HarvestReportReviewDTO(id, pointOfTime, creationTime, harvestReportDate, species, rkaName,
                rhyName, authorHunterNumber, shooterHunterNumber, permitType, permitNumber, partnerOfficialCode,
                partner, createdByModerator);
    }
}