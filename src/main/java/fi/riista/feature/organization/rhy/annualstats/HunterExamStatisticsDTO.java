package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;

public class HunterExamStatisticsDTO {

    public static HunterExamStatisticsDTO create(final HunterExamStatistics entity) {
        final HunterExamStatisticsDTO dto = new HunterExamStatisticsDTO();

        if (entity.isHunterExamEventsManuallyOverridden()) {
            dto.setModeratorOverriddenHunterExamEvents(entity.getHunterExamEvents());
        } else {
            dto.setHunterExamEvents(entity.getHunterExamEvents());
        }

        dto.setPassedHunterExams(entity.getPassedHunterExams());
        dto.setFailedHunterExams(entity.getFailedHunterExams());
        dto.setHunterExamAttemptResultsOverridden(entity.isHunterExamAttemptResultsOverridden());

        dto.setHunterExamOfficials(entity.getHunterExamOfficials());
        dto.setLastModified(entity.getLastModified());
        return dto;
    }

    // Metsästäjätutkintotilaisuuksien määrä
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer hunterExamEvents;

    // Moderaattorin ylimäärittelemä metsästäjätutkintotilaisuuksien määrä
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    private Integer moderatorOverriddenHunterExamEvents;

    // Hyväksyttyjen metsästäjätutkintoyritysten määrä
    @Min(0)
    private Integer passedHunterExams;

    // Hylättyjen metsästäjätutkintoyritysten määrä
    @Min(0)
    private Integer failedHunterExams;

    private boolean hunterExamAttemptResultsOverridden;

    // Metsästäjätutkinnon vastaanottajien määrä
    @Min(0)
    private Integer hunterExamOfficials;

    private DateTime lastModified;

    @AssertTrue
    public boolean isHunterExamEventsDefinedExclusively() {
        return hunterExamEvents == null || moderatorOverriddenHunterExamEvents == null;
    }

    // Accessors -->

    public Integer getHunterExamEvents() {
        return hunterExamEvents;
    }

    public void setHunterExamEvents(final Integer hunterExamEvents) {
        this.hunterExamEvents = hunterExamEvents;
    }

    public Integer getModeratorOverriddenHunterExamEvents() {
        return moderatorOverriddenHunterExamEvents;
    }

    public void setModeratorOverriddenHunterExamEvents(final Integer hunterExamEventsOverriddenByModerator) {
        this.moderatorOverriddenHunterExamEvents = hunterExamEventsOverriddenByModerator;
    }

    public Integer getPassedHunterExams() {
        return passedHunterExams;
    }

    public void setPassedHunterExams(final Integer passedHunterExams) {
        this.passedHunterExams = passedHunterExams;
    }

    public Integer getFailedHunterExams() {
        return failedHunterExams;
    }

    public void setFailedHunterExams(final Integer failedHunterExams) {
        this.failedHunterExams = failedHunterExams;
    }

    public boolean isHunterExamAttemptResultsOverridden() {
        return hunterExamAttemptResultsOverridden;
    }

    public void setHunterExamAttemptResultsOverridden(final boolean hunterExamAttemptResultsOverridden) {
        this.hunterExamAttemptResultsOverridden = hunterExamAttemptResultsOverridden;
    }

    public Integer getHunterExamOfficials() {
        return hunterExamOfficials;
    }

    public void setHunterExamOfficials(final Integer hunterExamOfficials) {
        this.hunterExamOfficials = hunterExamOfficials;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
