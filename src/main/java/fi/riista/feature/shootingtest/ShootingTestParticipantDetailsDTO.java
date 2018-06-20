package fi.riista.feature.shootingtest;

import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;

public class ShootingTestParticipantDetailsDTO extends IdRevisionDTO {

    public static class AttemptDTO extends BaseEntityDTO<Long> {
        private Long id;
        private Integer rev;
        private ShootingTestType type;
        private ShootingTestAttemptResult result;
        private Integer hits;
        private String note;
        private String author;

        public static AttemptDTO create(@Nonnull final ShootingTestAttempt attempt) {
            final AttemptDTO dto = new AttemptDTO();
            DtoUtil.copyBaseFields(attempt, dto);

            dto.setType(attempt.getType());
            dto.setResult(attempt.getResult());
            dto.setHits(attempt.getHits());
            dto.setNote(attempt.getNote());
            return dto;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(final Long id) {
            this.id = id;
        }

        @Override
        public Integer getRev() {
            return rev;
        }

        @Override
        public void setRev(final Integer rev) {
            this.rev = rev;
        }

        public ShootingTestType getType() {
            return type;
        }

        public void setType(final ShootingTestType type) {
            this.type = type;
        }

        public ShootingTestAttemptResult getResult() {
            return result;
        }

        public void setResult(final ShootingTestAttemptResult result) {
            this.result = result;
        }

        public Integer getHits() {
            return hits;
        }

        public void setHits(final Integer hits) {
            this.hits = hits;
        }

        public String getNote() {
            return note;
        }

        public void setNote(final String note) {
            this.note = note;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(final String author) {
            this.author = author;
        }
    }

    private String firstName;
    private String lastName;
    private String hunterNumber;
    private LocalDate dateOfBirth;

    private boolean mooseTestIntended;
    private boolean bearTestIntended;
    private boolean deerTestIntended;
    private boolean bowTestIntended;

    private DateTime registrationTime;
    private boolean completed;

    private List<AttemptDTO> attempts;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isMooseTestIntended() {
        return mooseTestIntended;
    }

    public void setMooseTestIntended(final boolean mooseTestIntended) {
        this.mooseTestIntended = mooseTestIntended;
    }

    public boolean isBearTestIntended() {
        return bearTestIntended;
    }

    public void setBearTestIntended(final boolean bearTestIntended) {
        this.bearTestIntended = bearTestIntended;
    }

    public boolean isDeerTestIntended() {
        return deerTestIntended;
    }

    public void setDeerTestIntended(final boolean deerTestIntended) {
        this.deerTestIntended = deerTestIntended;
    }

    public boolean isBowTestIntended() {
        return bowTestIntended;
    }

    public void setBowTestIntended(final boolean bowTestIntended) {
        this.bowTestIntended = bowTestIntended;
    }

    public DateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(final DateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }

    public List<AttemptDTO> getAttempts() {
        return attempts;
    }

    public void setAttempts(final List<AttemptDTO> attempts) {
        this.attempts = attempts;
    }
}
