package fi.riista.feature.permit.application.amendment;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.validation.Valid;
import java.util.Optional;

public class HarvestPermitAmendmentApplicationDTO extends BaseEntityDTO<Long> {
    private Long id;
    private Integer rev;

    private long originalPermitId;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String originalPermitNumber;
    private HarvestPermitApplication.Status status;
    private Integer applicationNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String applicationName;

    private int huntingYear;

    private LocalDateTime submitDate;

    private Long nonEdibleHarvestId;

    private Integer gameSpeciesCode;

    private LocalDateTime pointOfTime;
    private GameAge age;
    private GameGender gender;

    @Valid
    private PersonWithHunterNumberDTO shooter;

    @Valid
    private OrganisationNameDTO partner;

    @Valid
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    public HarvestPermitAmendmentApplicationDTO() {
    }

    public HarvestPermitAmendmentApplicationDTO(final HarvestPermitApplication entity,
                                                final HarvestPermitApplicationSpeciesAmount spa,
                                                final AmendmentApplicationData data) {

        DtoUtil.copyBaseFields(entity, this);

        this.originalPermitId = data.getOriginalPermit().getId();
        this.originalPermitNumber = data.getOriginalPermit().getPermitNumber();
        this.status = entity.getStatus();
        this.applicationNumber = entity.getApplicationNumber();
        this.applicationName = entity.getApplicationName();
        this.huntingYear = entity.getApplicationYear();
        this.submitDate = entity.getSubmitDate() != null ? entity.getSubmitDate().toLocalDateTime() : null;

        this.nonEdibleHarvestId = F.getId(data.getNonEdibleHarvest());
        this.gameSpeciesCode = spa.getGameSpecies().getOfficialCode();
        this.pointOfTime = DateUtil.toLocalDateTimeNullSafe(data.getPointOfTime());
        this.age = data.getAge();
        this.gender = data.getGender();
        this.shooter = Optional.ofNullable(data.getShooter()).map(PersonWithHunterNumberDTO::create).orElse(null);
        this.partner = Optional.ofNullable(data.getPartner()).map(OrganisationNameDTO::create).orElse(null);
        this.geoLocation = data.getGeoLocation();
        this.description = spa.getMooselikeDescription();
    }

    public HarvestPermitAmendmentApplicationDTO(final Harvest harvest, final HarvestSpecimen specimen) {
        this.nonEdibleHarvestId = F.getId(harvest);
        this.gameSpeciesCode = harvest.getSpecies().getOfficialCode();
        this.pointOfTime = DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime());
        this.age = specimen.getAge();
        this.gender = specimen.getGender();
        this.shooter = PersonWithHunterNumberDTO.create(harvest.getActualShooter());
        this.geoLocation = harvest.getGeoLocation();
    }

    public HarvestPermitAmendmentApplicationDTO(final HarvestPermitApplication application,
                                                final AmendmentApplicationData data) {
        this(application, application.getSpeciesAmounts().get(0), data);
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return this.rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public long getOriginalPermitId() {
        return originalPermitId;
    }

    public void setOriginalPermitId(final long originalPermitId) {
        this.originalPermitId = originalPermitId;
    }

    public String getOriginalPermitNumber() {
        return originalPermitNumber;
    }

    public void setOriginalPermitNumber(final String originalPermitNumber) {
        this.originalPermitNumber = originalPermitNumber;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public void setStatus(final HarvestPermitApplication.Status status) {
        this.status = status;
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

    public Long getNonEdibleHarvestId() {
        return nonEdibleHarvestId;
    }

    public void setNonEdibleHarvestId(final Long nonEdibleHarvestId) {
        this.nonEdibleHarvestId = nonEdibleHarvestId;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(final GameAge age) {
        this.age = age;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    public PersonWithHunterNumberDTO getShooter() {
        return shooter;
    }

    public void setShooter(final PersonWithHunterNumberDTO shooter) {
        this.shooter = shooter;
    }

    public OrganisationNameDTO getPartner() {
        return partner;
    }

    public void setPartner(final OrganisationNameDTO partner) {
        this.partner = partner;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
