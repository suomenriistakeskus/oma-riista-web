package fi.riista.feature.gamediary.observation.metadata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fi.riista.config.Constants;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "observationSpecVersion", "lastModified" })
public class ObservationMetadataDTO {

    @JsonProperty(value = "observationSpecVersion")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObservationSpecVersion mobileApiObservationSpecVersion;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateUtil.TIMESTAMP_FORMAT_WITH_OFFSET_ZONE,
            timezone = Constants.DEFAULT_TIMEZONE_ID)
    private DateTime lastModified;

    private final List<GameSpeciesObservationFieldRequirementsDTO> speciesList = new ArrayList<>();

    public ObservationSpecVersion getMobileApiObservationSpecVersion() {
        return mobileApiObservationSpecVersion;
    }

    public void setMobileApiObservationSpecVersion(final ObservationSpecVersion mobileApiObservationSpecVersion) {
        this.mobileApiObservationSpecVersion = mobileApiObservationSpecVersion;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public List<GameSpeciesObservationFieldRequirementsDTO> getSpeciesList() {
        return speciesList;
    }

}
