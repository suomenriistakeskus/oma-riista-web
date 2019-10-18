package fi.riista.feature.permit.application.search;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import org.joda.time.LocalDateTime;

import java.util.Set;

public class HarvestPermitApplicationSearchResultDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    private HarvestPermitApplication.Status status;
    private boolean deliveryByMail;
    private int huntingYear;
    private LocalDateTime submitDate;
    private Integer applicationNumber;
    private HarvestPermitCategory harvestPermitCategory;
    private boolean hasPermitArea;
    private GISZoneSizeDTO areaSize;
    private Set<Integer> gameSpeciesCodes;

    private PersonWithNameDTO contactPerson;
    private PermitHolderDTO permitHolder;
    private OrganisationNameDTO huntingClub;

    private OrganisationNameDTO rhy;
    private PersonWithNameDTO handler;
    private PermitDecision.Status decisionStatus;

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

    public boolean isDeliveryByMail() {
        return deliveryByMail;
    }

    public void setDeliveryByMail(final boolean deliveryByMail) {
        this.deliveryByMail = deliveryByMail;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final LocalDateTime submitDate) {
        this.submitDate = submitDate;
    }

    public Set<Integer> getGameSpeciesCodes() {
        return gameSpeciesCodes;
    }

    public void setGameSpeciesCodes(final Set<Integer> gameSpeciesCodes) {
        this.gameSpeciesCodes = gameSpeciesCodes;
    }

    public boolean isHasPermitArea() {
        return hasPermitArea;
    }

    public void setHasPermitArea(final boolean hasPermitArea) {
        this.hasPermitArea = hasPermitArea;
    }

    public GISZoneSizeDTO getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final GISZoneSizeDTO areaSize) {
        this.areaSize = areaSize;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public PersonWithNameDTO getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final PersonWithNameDTO contactPerson) {
        this.contactPerson = contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolderDTO permitHolder) {
        this.permitHolder = permitHolder;
    }

    public OrganisationNameDTO getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(OrganisationNameDTO huntingClub) {
        this.huntingClub = huntingClub;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(final OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

    public PersonWithNameDTO getHandler() {
        return handler;
    }

    public void setHandler(final PersonWithNameDTO handler) {
        this.handler = handler;
    }

    public PermitDecision.Status getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(final PermitDecision.Status decisionStatus) {
        this.decisionStatus = decisionStatus;
    }
}
