package fi.riista.feature.permit.application.mooselike;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubSubtype;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

// Use this DTO to display full application summary
public class MooselikePermitApplicationSummaryDTO extends BaseEntityDTO<Long> {

    public static class PartnerDTO extends OrganisationNameDTO {
        private List<PersonWithNameDTO> contactPersons;

        PartnerDTO(final Organisation organisation,
                   final List<Person> contactPersons) {
            super(organisation);
            setOfficialCode(organisation.getOfficialCode());
            this.contactPersons = F.mapNonNullsToList(contactPersons, PersonWithNameDTO::create);
        }

        public List<PersonWithNameDTO> getContactPersons() {
            return contactPersons;
        }
    }

    public static class PermitHolderClubDTO extends OrganisationNameDTO {
        private final HuntingClubSubtype subtype;

        PermitHolderClubDTO(final HuntingClub club) {
            super(club);
            setOfficialCode(club.getOfficialCode());
            this.subtype = club.getSubtype();
        }

        public HuntingClubSubtype getSubtype() {
            return subtype;
        }
    }

    public static MooselikePermitApplicationSummaryDTO create(final HarvestPermitApplication entity,
                                                              final Map<Long, List<Person>> contactPersonMapping,
                                                              final String createdByModeratorName) {
        final MooselikePermitApplicationSummaryDTO dto = new MooselikePermitApplicationSummaryDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setCreatedByModeratorName(createdByModeratorName);

        dto.setApplicationNumber(entity.getApplicationNumber());
        dto.setApplicationName(entity.getApplicationName());
        dto.setHarvestPermitCategory(entity.getHarvestPermitCategory());
        dto.setContactPerson(Optional.ofNullable(entity.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null));
        dto.setPermitHolder(entity.getPermitHolder() != null
                ? fi.riista.feature.permit.application.PermitHolderDTO.createFrom(entity.getPermitHolder())
                : null);
        dto.setHuntingClub(entity.getHuntingClub() != null
                ? new PermitHolderClubDTO(entity.getHuntingClub())
                : null);
        dto.setPermitPartners(F.mapNonNullsToList(entity.getPermitPartners(), club -> new PartnerDTO(
                club, contactPersonMapping.getOrDefault(club.getId(), emptyList()))));

        dto.setAttachments(entity.getAttachments().stream()
                .map(HarvestPermitApplicationAttachmentDTO::new)
                .collect(Collectors.toList()));

        dto.setSpeciesAmounts(entity.getSpeciesAmounts().stream()
                .map(MooselikePermitApplicationSpeciesAmountDTO::create)
                .collect(Collectors.toList()));

        dto.setShooterOnlyClub(entity.getShooterOnlyClub());
        dto.setShooterOtherClubPassive(entity.getShooterOtherClubPassive());
        dto.setShooterOtherClubActive(entity.getShooterOtherClubActive());

        dto.setEmail1(entity.getEmail1());
        dto.setEmail2(entity.getEmail2());

        dto.setDeliveryByMail(entity.getDeliveryByMail());

        dto.setDecisionLanguage(entity.getDecisionLocale().getLanguage());

        dto.setHuntingYear(entity.getApplicationYear());
        dto.setSubmitDate(entity.getSubmitDate() != null ? entity.getSubmitDate().toLocalDateTime() : null);

        dto.setStatus(entity.getStatus());

        return dto;
    }

    private Long id;
    private Integer rev;

    private String createdByModeratorName;

    private Integer applicationNumber;
    private String applicationName;
    private HarvestPermitCategory harvestPermitCategory;

    private PersonContactInfoDTO contactPerson;
    private PermitHolderDTO permitHolder;
    private PermitHolderClubDTO huntingClub;
    private List<PartnerDTO> permitPartners;

    private List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts;
    private List<HarvestPermitApplicationAttachmentDTO> attachments;

    private Integer shooterOnlyClub;
    private Integer shooterOtherClubPassive;
    private Integer shooterOtherClubActive;

    private String email1;
    private String email2;

    private Boolean deliveryByMail;

    private String decisionLanguage;

    private int huntingYear;
    private LocalDateTime submitDate;

    private HarvestPermitApplication.Status status;

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

    public String getCreatedByModeratorName() {
        return createdByModeratorName;
    }

    public void setCreatedByModeratorName(final String createdByModeratorName) {
        this.createdByModeratorName = createdByModeratorName;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonContactInfoDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolderDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public PermitHolderClubDTO getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(PermitHolderClubDTO huntingClub) {
        this.huntingClub = huntingClub;
    }

    public List<PartnerDTO> getPermitPartners() {
        return permitPartners;
    }

    public void setPermitPartners(final List<PartnerDTO> permitPartners) {
        this.permitPartners = permitPartners;
    }

    public List<HarvestPermitApplicationAttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<HarvestPermitApplicationAttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<MooselikePermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public Integer getShooterOnlyClub() {
        return shooterOnlyClub;
    }

    public void setShooterOnlyClub(final Integer shooterOnlyClub) {
        this.shooterOnlyClub = shooterOnlyClub;
    }

    public Integer getShooterOtherClubPassive() {
        return shooterOtherClubPassive;
    }

    public void setShooterOtherClubPassive(final Integer shooterOtherClubPassive) {
        this.shooterOtherClubPassive = shooterOtherClubPassive;
    }

    public Integer getShooterOtherClubActive() {
        return shooterOtherClubActive;
    }

    public void setShooterOtherClubActive(final Integer shooterOtherClubActive) {
        this.shooterOtherClubActive = shooterOtherClubActive;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(final String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(final String email2) {
        this.email2 = email2;
    }

    public Boolean getDeliveryByMail() {
        return deliveryByMail;
    }

    public void setDeliveryByMail(final Boolean deliveryByMail) {
        this.deliveryByMail = deliveryByMail;
    }

    public String getDecisionLanguage() {
        return decisionLanguage;
    }

    public void setDecisionLanguage(String decisionLanguage) {
        this.decisionLanguage = decisionLanguage;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final LocalDateTime submitDate) {
        this.submitDate = submitDate;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public void setStatus(final HarvestPermitApplication.Status status) {
        this.status = status;
    }
}
