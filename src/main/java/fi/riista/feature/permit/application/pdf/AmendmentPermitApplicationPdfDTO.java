package fi.riista.feature.permit.application.pdf;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.decision.PermitDecisionDocumentTransformer;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.List;

public class AmendmentPermitApplicationPdfDTO {
    private final int huntingYear;
    private final DateTime submitDate;
    private final Integer applicationNumber;
    private final String applicationName;

    private final PersonContactInfoDTO contactPerson;
    private final PermitHolderDTO permitHolder;
    private final String originalPermitNumber;
    private final String species;
    private final LocalDateTime pointOfTime;
    private final String age;
    private final String gender;
    private final PersonContactInfoDTO shooter;
    private final OrganisationNameDTO partner;
    private final GeoLocation geoLocation;
    private final String description;


    private final List<String> officialStatements;
    private final List<String> otherAttachments;

    public AmendmentPermitApplicationPdfDTO(final HarvestPermitApplication application,
                                            final AmendmentApplicationData data) {

        final HarvestPermitApplicationSpeciesAmount spa = application.getSpeciesAmounts().get(0);

        this.huntingYear = application.getApplicationYear();
        this.submitDate = application.getSubmitDate();
        this.applicationNumber = application.getApplicationNumber();
        this.applicationName = application.getApplicationName();

        this.contactPerson = PersonContactInfoDTO.create(application.getContactPerson());
        this.permitHolder = PermitHolderDTO.createFrom(application.getPermitHolder());
        this.originalPermitNumber = data.getOriginalPermit().getPermitNumber();
        this.species = spa.getGameSpecies().getNameFinnish();

        this.pointOfTime = DateUtil.toLocalDateTimeNullSafe(data.getPointOfTime());
        this.age = data.getAge() == GameAge.ADULT ? "Aikuinen" : "Alle 1 v";
        this.gender = data.getGender() == GameGender.MALE ? "Uros" : "Naaras";
        this.shooter = data.getShooter() != null ? PersonContactInfoDTO.create(data.getShooter()) : null;
        this.partner = OrganisationNameDTO.create(data.getPartner());
        this.geoLocation = data.getGeoLocation();
        this.description = spa.getMooselikeDescription() != null
                ? PermitDecisionDocumentTransformer.MARKDOWN_TO_HTML.transform(spa.getMooselikeDescription()) : "-";

        this.officialStatements = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.OFFICIAL_STATEMENT);
        this.otherAttachments = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.OTHER);
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public DateTime getSubmitDate() {
        return submitDate;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public String getOriginalPermitNumber() {
        return originalPermitNumber;
    }

    public String getSpecies() {
        return species;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public PersonContactInfoDTO getShooter() {
        return shooter;
    }

    public OrganisationNameDTO getPartner() {
        return partner;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOfficialStatements() {
        return officialStatements;
    }

    public List<String> getOtherAttachments() {
        return otherAttachments;
    }
}
