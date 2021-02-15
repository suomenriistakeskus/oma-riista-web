package fi.riista.feature.permit.decision;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.decision.AppealStatus;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.constraints.NotNull;

public class DecisionPublishSettingsDTO {

    @NotNull
    private Long decisionId;

    @NotNull
    private LocalDate publishDate;

    private AppealStatus appealStatus;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime publishTime;

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(final Long decisionId) {
        this.decisionId = decisionId;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(final LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public LocalTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(final LocalTime publishTime) {
        this.publishTime = publishTime;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }
}
