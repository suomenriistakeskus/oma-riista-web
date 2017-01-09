package fi.riista.feature.gamediary.observation.metadata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fi.riista.config.Constants;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;

@JsonPropertyOrder({ "observationSpecVersion", "lastModified", "gameSpeciesId", "gameSpeciesCode", "baseFields", "specimenFields", "contextSensitiveFieldSets" })
public class GameSpeciesObservationMetadataDTO extends GameSpeciesObservationFieldRequirementsDTO {

    private final long gameSpeciesId;

    @JsonProperty(value = "observationSpecVersion")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer mobileApiObservationSpecVersion;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateUtil.TIMESTAMP_FORMAT_WITH_OFFSET_ZONE,
            timezone = Constants.DEFAULT_TIMEZONE_ID)
    private DateTime lastModified;

    public GameSpeciesObservationMetadataDTO(final long gameSpeciesId, final int gameSpeciesCode) {
        super(gameSpeciesCode);
        this.gameSpeciesId = gameSpeciesId;
    }

    // Accessors -->

    public long getGameSpeciesId() {
        return gameSpeciesId;
    }

    public Integer getMobileApiObservationSpecVersion() {
        return mobileApiObservationSpecVersion;
    }

    public void setMobileApiObservationSpecVersion(Integer mobileApiObservationSpecVersion) {
        this.mobileApiObservationSpecVersion = mobileApiObservationSpecVersion;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

}
