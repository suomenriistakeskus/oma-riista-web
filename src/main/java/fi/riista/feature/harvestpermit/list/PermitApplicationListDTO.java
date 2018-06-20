package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmountDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;

public class PermitApplicationListDTO extends BaseEntityDTO<Long> {

    public static PermitApplicationListDTO create(final HarvestPermitApplication application) {
        final PermitApplicationListDTO dto = new PermitApplicationListDTO();
        DtoUtil.copyBaseFields(application, dto);
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setApplicationName(application.getApplicationName());
        dto.setPermitTypeCode(application.getPermitTypeCode());
        dto.setStatus(application.getStatus());
        dto.setSubmitDate(application.getSubmitDate());

        if (application.getPermitHolder() != null) {
            dto.setPermitHolder(OrganisationNameDTO.createWithOfficialCode(application.getPermitHolder()));
        }

        dto.setContactPerson(HarvestPermitContactPersonDTO.create(application.getContactPerson()));
        dto.setSpeciesAmounts(application.getSpeciesAmounts().stream()
                .map(HarvestPermitApplicationSpeciesAmountDTO::create)
                .collect(Collectors.toList()));

        if (application.getArea() != null) {
            dto.setAreaExternalId(application.getArea().getExternalId());
        }

        return dto;
    }

    private Long id;
    private Integer rev;

    private HarvestPermitApplication.Status status;
    private DateTime submitDate;

    private Integer applicationNumber;
    private String applicationName;
    private String permitTypeCode;
    private String areaExternalId;

    private HarvestPermitContactPersonDTO contactPerson;
    private OrganisationNameDTO permitHolder;

    private List<HarvestPermitApplicationSpeciesAmountDTO> speciesAmounts;

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

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public void setStatus(final HarvestPermitApplication.Status status) {
        this.status = status;
    }

    public DateTime getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final DateTime submitDate) {
        this.submitDate = submitDate;
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

    public String getAreaExternalId() {
        return areaExternalId;
    }

    public void setAreaExternalId(final String areaExternalId) {
        this.areaExternalId = areaExternalId;
    }

    public HarvestPermitContactPersonDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final HarvestPermitContactPersonDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public OrganisationNameDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final OrganisationNameDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public List<HarvestPermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<HarvestPermitApplicationSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }
}
