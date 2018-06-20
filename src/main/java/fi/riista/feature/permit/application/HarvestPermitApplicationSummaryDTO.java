package fi.riista.feature.permit.application;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubSubtype;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmountDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

// Use this DTO to display full application summary
public class HarvestPermitApplicationSummaryDTO extends BaseEntityDTO<Long> {

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

    public static class PermitHolderDTO extends OrganisationNameDTO {
        private final HuntingClubSubtype subtype;

        PermitHolderDTO(final HuntingClub club) {
            super(club);
            setOfficialCode(club.getOfficialCode());
            this.subtype = club.getSubtype();
        }

        public HuntingClubSubtype getSubtype() {
            return subtype;
        }
    }

    public static HarvestPermitApplicationSummaryDTO create(final HarvestPermitApplication entity,
                                                            final Map<Long, List<Person>> contactPersonMapping,
                                                            final String createdByModeratorName) {
        final HarvestPermitApplicationSummaryDTO dto = new HarvestPermitApplicationSummaryDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setCreatedByModeratorName(createdByModeratorName);

        dto.setApplicationNumber(entity.getApplicationNumber());
        dto.setApplicationName(entity.getApplicationName());
        dto.setPermitTypeCode(entity.getPermitTypeCode());
        dto.setContactPerson(Optional.ofNullable(entity.getContactPerson())
                .map(PersonContactInfoDTO::create)
                .orElse(null));
        dto.setPermitHolder(entity.getPermitHolder() != null ? new PermitHolderDTO(entity.getPermitHolder()) : null);
        dto.setPermitPartners(F.mapNonNullsToList(entity.getPermitPartners(), club -> new PartnerDTO(
                club, contactPersonMapping.getOrDefault(club.getId(), emptyList()))));

        dto.setAttachments(entity.getAttachments().stream()
                .map(HarvestPermitApplicationAttachmentDTO::new)
                .collect(Collectors.toList()));

        dto.setSpeciesAmounts(entity.getSpeciesAmounts().stream()
                .map(HarvestPermitApplicationSpeciesAmountDTO::create)
                .collect(Collectors.toList()));

        dto.setShooterOnlyClub(entity.getShooterOnlyClub());
        dto.setShooterOtherClubPassive(entity.getShooterOtherClubPassive());
        dto.setShooterOtherClubActive(entity.getShooterOtherClubActive());

        dto.setEmail1(entity.getEmail1());
        dto.setEmail2(entity.getEmail2());

        dto.setDeliveryByMail(entity.getDeliveryByMail());

        dto.setHuntingYear(entity.getHuntingYear());
        dto.setSubmitDate(entity.getSubmitDate() != null ? entity.getSubmitDate().toLocalDateTime() : null);

        dto.setStatus(entity.getStatus());

        return dto;
    }

    private Long id;
    private Integer rev;

    private String createdByModeratorName;

    private Integer applicationNumber;
    private String applicationName;
    private String permitTypeCode;

    private PersonContactInfoDTO contactPerson;
    private PermitHolderDTO permitHolder;
    private List<PartnerDTO> permitPartners;

    private List<HarvestPermitApplicationSpeciesAmountDTO> speciesAmounts;
    private List<HarvestPermitApplicationAttachmentDTO> attachments;

    private Integer shooterOnlyClub;
    private Integer shooterOtherClubPassive;
    private Integer shooterOtherClubActive;

    private String email1;
    private String email2;

    private Boolean deliveryByMail;

    private int huntingYear;
    private LocalDateTime submitDate;

    private HarvestPermitApplication.Status status;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
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

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
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

    public List<PartnerDTO> getPermitPartners() {
        return permitPartners;
    }

    public void setPermitPartners(List<PartnerDTO> permitPartners) {
        this.permitPartners = permitPartners;
    }

    public List<HarvestPermitApplicationAttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<HarvestPermitApplicationAttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<HarvestPermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<HarvestPermitApplicationSpeciesAmountDTO> speciesAmounts) {
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
