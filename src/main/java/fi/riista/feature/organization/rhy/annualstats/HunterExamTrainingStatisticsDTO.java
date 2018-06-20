package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;

public class HunterExamTrainingStatisticsDTO {

    public static HunterExamTrainingStatisticsDTO create(final HunterExamTrainingStatistics entity) {
        final HunterExamTrainingStatisticsDTO dto = new HunterExamTrainingStatisticsDTO();

        if (entity.isHunterExamTrainingEventsManuallyOverridden()) {
            dto.setModeratorOverriddenHunterExamTrainingEvents(entity.getHunterExamTrainingEvents());
        } else {
            dto.setHunterExamTrainingEvents(entity.getHunterExamTrainingEvents());
        }

        dto.setHunterExamTrainingParticipants(entity.getHunterExamTrainingParticipants());
        dto.setLastModified(entity.getLastModified());
        return dto;
    }

    // Metsästäjätutkintoon valmistavat koulutustilaisuudet, lkm
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer hunterExamTrainingEvents;

    // Metsästäjätutkintoon valmistavat koulutustilaisuudet, lkm, moderaattorin ylimäärittelemä
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer moderatorOverriddenHunterExamTrainingEvents;

    // Metsästäjätutkintoon valmistavien koulutusten osallistujat, lkm
    @Min(0)
    private Integer hunterExamTrainingParticipants;

    private DateTime lastModified;

    @AssertTrue
    public boolean isHunterExamTrainingEventsDefinedExclusively() {
        return hunterExamTrainingEvents == null || moderatorOverriddenHunterExamTrainingEvents == null;
    }

    // Accessors -->

    public Integer getHunterExamTrainingEvents() {
        return hunterExamTrainingEvents;
    }

    public void setHunterExamTrainingEvents(final Integer hunterExamTrainingEvents) {
        this.hunterExamTrainingEvents = hunterExamTrainingEvents;
    }

    public Integer getModeratorOverriddenHunterExamTrainingEvents() {
        return moderatorOverriddenHunterExamTrainingEvents;
    }

    public void setModeratorOverriddenHunterExamTrainingEvents(final Integer moderatorOverriddenHunterExamTrainingEvents) {
        this.moderatorOverriddenHunterExamTrainingEvents = moderatorOverriddenHunterExamTrainingEvents;
    }

    public Integer getHunterExamTrainingParticipants() {
        return hunterExamTrainingParticipants;
    }

    public void setHunterExamTrainingParticipants(final Integer hunterExamTrainingParticipants) {
        this.hunterExamTrainingParticipants = hunterExamTrainingParticipants;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
