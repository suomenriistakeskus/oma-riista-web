package fi.riista.feature.shootingtest;

import fi.riista.feature.common.dto.IdRevisionDTO;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

public class ShootingTestParticipantDTO extends IdRevisionDTO {

    public static class AttemptDTO {

        private ShootingTestType type;
        private int attemptCount;
        private boolean qualified;

        public AttemptDTO(final ShootingTestType type, final int attemptCount, final boolean qualified) {
            this.type = type;
            this.attemptCount = attemptCount;
            this.qualified = qualified;
        }

        public ShootingTestType getType() {
            return type;
        }

        public void setType(final ShootingTestType type) {
            this.type = type;
        }

        public int getAttemptCount() {
            return attemptCount;
        }

        public void setAttemptCount(final int attemptCount) {
            this.attemptCount = attemptCount;
        }

        public boolean isQualified() {
            return qualified;
        }

        public void setQualified(final boolean qualified) {
            this.qualified = qualified;
        }
    }

    private String firstName;
    private String lastName;
    private String hunterNumber;

    private boolean mooseTestIntended;
    private boolean bearTestIntended;
    private boolean deerTestIntended;
    private boolean bowTestIntended;

    private List<AttemptDTO> attempts;

    private BigDecimal totalDueAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    private DateTime registrationTime;
    private boolean completed;

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

    public List<AttemptDTO> getAttempts() {
        return attempts;
    }

    public void setAttempts(final List<AttemptDTO> attempts) {
        this.attempts = attempts;
    }

    public BigDecimal getTotalDueAmount() {
        return totalDueAmount;
    }

    public void setTotalDueAmount(final BigDecimal totalDueAmount) {
        this.totalDueAmount = totalDueAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(final BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(final BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
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
}
