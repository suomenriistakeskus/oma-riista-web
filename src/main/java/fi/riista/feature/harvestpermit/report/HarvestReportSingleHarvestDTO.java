package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.harvestpermit.season.HarvestQuotaDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.gamediary.harvest.HarvestLukeStatus;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.util.DateUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HarvestReportSingleHarvestDTO extends HarvestReportDTOBase {


    @Nonnull
    public static HarvestReportDTOBase create(@Nonnull final HarvestReport report,
                                              final SystemUser user) {
        return create(report, user, Collections.emptyMap());
    }

    @Nonnull
    public static HarvestReportSingleHarvestDTO create(@Nonnull final HarvestReport report,
                                                       final SystemUser user,
                                                       final Map<Long, SystemUser> moderatorCreators) {

        final HarvestReportSingleHarvestDTO dto = new HarvestReportSingleHarvestDTO();
        HarvestReportDTOBase.copyBaseFields(report, dto, user, moderatorCreators);

        if (report.getHarvestPermit() != null) {
            dto.setPermitNumber(report.getHarvestPermit().getPermitNumber());
            dto.setRhyId(report.getHarvestPermit().getRhy().getId());
        }

        if (!report.getHarvests().isEmpty()) {

            Harvest harvest = report.getHarvests().iterator().next();
            HarvestSpecimen specimen = harvest.getSortedSpecimens().iterator().next();

            dto.setGameDiaryEntryId(harvest.getId());
            dto.setGameDiaryEntryRev(harvest.getConsistencyVersion());
            dto.setGameSpeciesCode(harvest.getSpecies().getOfficialCode());

            dto.setAuthorInfo(PersonWithHunterNumberDTO.create(report.getAuthor()));
            dto.setHunterInfo(PersonWithHunterNumberDTO.create(harvest.getActualShooter()));

            if (harvest.getHarvestSeason() != null) {
                dto.setHarvestSeason(HarvestSeasonDTO.create(harvest.getHarvestSeason()));
            }

            if (harvest.getHarvestQuota() != null) {
                dto.setHarvestQuota(HarvestQuotaDTO.create(harvest.getHarvestQuota()));
            }

            if (harvest.getHarvestReportFields() != null) {
                dto.setFields(HarvestReportFieldsDTO.create(harvest.getHarvestReportFields()));
            }
            if (harvest.getRhy() != null) {
                dto.setRhyId(harvest.getRhy().getId());
            }

            if (harvest.getPropertyIdentifier() != null) {
                dto.setPropertyIdentifier(harvest.getPropertyIdentifier().getValue());
            }

            dto.setGeoLocation(harvest.getGeoLocation());
            dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()));

            dto.setAge(specimen.getAge());
            dto.setGender(specimen.getGender());
            dto.setWeight(specimen.getWeight());

            dto.setHuntingAreaType(harvest.getHuntingAreaType());
            dto.setHuntingParty(harvest.getHuntingParty());
            dto.setHuntingAreaSize(harvest.getHuntingAreaSize());
            dto.setHuntingMethod(harvest.getHuntingMethod());
            dto.setReportedWithPhoneCall(harvest.getReportedWithPhoneCall());
            dto.setLukeStatus(harvest.getLukeStatus());
        }

        return dto;
    }

    private Long gameDiaryEntryId;
    private Integer gameDiaryEntryRev;
    private int gameSpeciesCode;
    @DoNotValidate
    private HarvestSeasonDTO harvestSeason;
    @DoNotValidate
    private HarvestQuotaDTO harvestQuota;
    @Valid
    private HarvestReportFieldsDTO fields;
    @Valid
    private PersonWithHunterNumberDTO hunterInfo;
    private List<HarvestReport.State> transitions;

    private GeoLocation geoLocation;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String propertyIdentifier;

    private LocalDateTime pointOfTime;
    private GameAge age;
    private GameGender gender;
    private Double weight;

    private HuntingAreaType huntingAreaType;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String huntingParty;
    private Double huntingAreaSize;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;
    private HuntingMethod huntingMethod;
    private Boolean reportedWithPhoneCall;
    private HarvestLukeStatus lukeStatus;

    @AssertTrue
    public boolean isPointOfTimeInPast() {
        return pointOfTime.isBefore(DateUtil.localDateTime());
    }

    public void setGameDiaryEntryId(Long gameDiaryEntryId) {
        this.gameDiaryEntryId = gameDiaryEntryId;
    }

    public Long getGameDiaryEntryId() {
        return gameDiaryEntryId;
    }

    public void setGameDiaryEntryRev(Integer harvestRev) {
        this.gameDiaryEntryRev = harvestRev;
    }

    public Integer getGameDiaryEntryRev() {
        return gameDiaryEntryRev;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public void setHarvestSeason(HarvestSeasonDTO harvestSeason) {
        this.harvestSeason = harvestSeason;
    }

    public HarvestSeasonDTO getHarvestSeason() {
        return harvestSeason;
    }

    public HarvestQuotaDTO getHarvestQuota() {
        return harvestQuota;
    }

    public void setHarvestQuota(HarvestQuotaDTO harvestQuota) {
        this.harvestQuota = harvestQuota;
    }

    public HarvestReportFieldsDTO getFields() {
        return fields;
    }

    public void setFields(HarvestReportFieldsDTO fields) {
        this.fields = fields;
    }

    public void setHunterInfo(PersonWithHunterNumberDTO hunterInfo) {
        this.hunterInfo = hunterInfo;
    }

    public PersonWithHunterNumberDTO getHunterInfo() {
        return hunterInfo;
    }

    @Override
    public List<HarvestReport.State> getTransitions() {
        return transitions;
    }

    @Override
    public void setTransitions(List<HarvestReport.State> transitions) {
        this.transitions = transitions;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public void setPropertyIdentifier(String propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    public void setPointOfTime(LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(GameAge age) {
        this.age = age;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(GameGender gender) {
        this.gender = gender;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public HuntingAreaType getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(HuntingAreaType huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    public String getHuntingParty() {
        return huntingParty;
    }

    public void setHuntingParty(String huntingParty) {
        this.huntingParty = huntingParty;
    }

    public Double getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(Double huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public HuntingMethod getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(HuntingMethod huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Boolean getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }

    public void setReportedWithPhoneCall(Boolean reportedWithPhoneCall) {
        this.reportedWithPhoneCall = reportedWithPhoneCall;
    }

    public HarvestLukeStatus getLukeStatus() {
        return lukeStatus;
    }

    public void setLukeStatus(HarvestLukeStatus lukeStatus) {
        this.lukeStatus = lukeStatus;
    }
}
