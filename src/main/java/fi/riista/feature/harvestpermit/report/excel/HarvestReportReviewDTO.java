package fi.riista.feature.harvestpermit.report.excel;

import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

public class HarvestReportReviewDTO {

    public HarvestReportReviewDTO(final long id, final LocalDateTime pointOfTime,
                                  final LocalDateTime creationTime, final LocalDateTime harvestReportDate,
                                  final LocalisedString species, final LocalisedString rkaName,
                                  final LocalisedString rhyName, final String authorHunterNumber,
                                  final String shooterHunterNumber, final String permitType,
                                  final String permitNumber, final String partnerOfficialCode,
                                  final LocalisedString partner, final boolean createdByModerator) {

        this.id = id;
        this.pointOfTime = pointOfTime;
        this.creationTime = creationTime;
        this.harvestReportDate = harvestReportDate;
        this.species = species;
        this.rkaName = rkaName;
        this.rhyName = rhyName;
        this.authorHunterNumber = authorHunterNumber;
        this.shooterHunterNumber = shooterHunterNumber;
        this.permitType = permitType;
        this.permitNumber = permitNumber;
        this.partnerOfficialCode = partnerOfficialCode;
        this.partner = partner;
        this.createdByModerator = createdByModerator;
    }

    private final long id;
    private final LocalDateTime pointOfTime;
    private final LocalDateTime creationTime;
    private final LocalDateTime harvestReportDate;

    private final LocalisedString species;
    private final LocalisedString rkaName;
    private final LocalisedString rhyName;
    private final String authorHunterNumber;
    private final String shooterHunterNumber;

    private final String permitType;

    private final String permitNumber;
    private final String partnerOfficialCode;
    private final LocalisedString partner;
    private final boolean createdByModerator;

    public long getId() {
        return id;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public LocalDateTime getHarvestReportDate() {
        return harvestReportDate;
    }

    public LocalisedString getSpecies() {
        return species;
    }

    public LocalisedString getRkaName() {
        return rkaName;
    }

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public String getAuthorHunterNumber() {
        return authorHunterNumber;
    }

    public String getShooterHunterNumber() {
        return shooterHunterNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getPartnerOfficialCode() {
        return partnerOfficialCode;
    }

    public LocalisedString getPartner() {
        return partner;
    }

    public boolean isCreatedByModerator() {
        return createdByModerator;
    }
}

