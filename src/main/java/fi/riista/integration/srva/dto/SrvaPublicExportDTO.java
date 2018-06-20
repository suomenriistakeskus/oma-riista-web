package fi.riista.integration.srva.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.List;

public class SrvaPublicExportDTO implements HasID<Long> {

    public static SrvaPublicExportDTO create(@Nonnull final SrvaEvent entity,
                                             final Integer gameSpeciesCode,
                                             final List<SrvaSpecimenDTO> specimens) {

        SrvaPublicExportDTO dto = new SrvaPublicExportDTO();

        dto.setId(entity.getId());
        dto.setRev(entity.getConsistencyVersion());
        dto.setPointOfTime(new DateTime(entity.getPointOfTime()));
        dto.setEventName(entity.getEventName());
        dto.setEventType(entity.getEventType());
        dto.setTotalSpecimenAmount(entity.getTotalSpecimenAmount());
        dto.setOtherTypeDescription(entity.getOtherTypeDescription());
        dto.setGeoLocation(entity.getGeoLocation());
        dto.setOtherSpeciesDescription(entity.getOtherSpeciesDescription());
        dto.setGameSpeciesCode(gameSpeciesCode);
        dto.setSpecimens(specimens);

        return dto;
    }

    @NotNull
    private long id;

    @NotNull
    private int rev;

    @NotNull
    @JsonFormat(timezone = Constants.DEFAULT_TIMEZONE_ID)
    private DateTime pointOfTime;

    @NotNull
    private SrvaEventNameEnum eventName;

    @NotNull
    private SrvaEventTypeEnum eventType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String otherTypeDescription;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer gameSpeciesCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String otherSpeciesDescription;

    @NotNull
    @JsonIgnoreProperties({"altitude", "altitudeAccuracy", "source", "accuracy"})
    private GeoLocation geoLocation;

    @NotNull
    private int totalSpecimenAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SrvaSpecimenDTO> specimens;


    //Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public SrvaEventNameEnum getEventName() {
        return eventName;
    }

    public void setEventName(SrvaEventNameEnum eventName) {
        this.eventName = eventName;
    }

    public SrvaEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(SrvaEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public int getTotalSpecimenAmount() {
        return totalSpecimenAmount;
    }

    public void setTotalSpecimenAmount(int totalSpecimenAmount) {
        this.totalSpecimenAmount = totalSpecimenAmount;
    }

    public List<SrvaSpecimenDTO> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(List<SrvaSpecimenDTO> specimens) {
        this.specimens = specimens;
    }

    public String getOtherTypeDescription() {
        return otherTypeDescription;
    }

    public void setOtherTypeDescription(String otherTypeDescription) {
        this.otherTypeDescription = otherTypeDescription;
    }

    public String getOtherSpeciesDescription() {
        return otherSpeciesDescription;
    }

    public void setOtherSpeciesDescription(String otherSpeciesDescription) {
        this.otherSpeciesDescription = otherSpeciesDescription;
    }
}
