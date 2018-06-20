package fi.riista.feature.shootingtest;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.BigDecimalComparison;
import fi.riista.util.jpa.CriteriaUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static fi.riista.feature.shootingtest.ShootingTestAttempt.calculatePaymentSum;

@Entity
@Access(value = AccessType.FIELD)
public class ShootingTestParticipant extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ShootingTestEvent shootingTestEvent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person person;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date registrationTime;

    @Column(nullable = false)
    private boolean mooseTestIntended;

    @Column(nullable = false)
    private boolean bearTestIntended;

    @Column(nullable = false)
    private boolean deerTestIntended;

    @Column(nullable = false)
    private boolean bowTestIntended;

    @Min(0)
    @Column
    private BigDecimal totalDueAmount;

    @Min(0)
    @Column
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private boolean completed;

    @OneToMany(mappedBy = "participant")
    private Set<ShootingTestAttempt> attempts = new HashSet<>();

    ShootingTestParticipant() {
    }

    public ShootingTestParticipant(final ShootingTestEvent shootingTestEvent, final Person person) {
        setShootingTestEvent(Objects.requireNonNull(shootingTestEvent, "shootingTestEvent is null"));
        setPerson(Objects.requireNonNull(person, "person is null"));

        reRegister();
    }

    @AssertTrue
    public boolean isPaymentStateValid() {
        if (paidAmount == null) {
            return !completed;
        }

        return totalDueAmount != null && totalDueAmount.compareTo(paidAmount) >= 0;
    }

    public BigDecimal getTotalDueAmountOrZero() {
        return totalDueAmount == null ? BigDecimal.ZERO : totalDueAmount;
    }

    public BigDecimal getPaidAmountOrZero() {
        return paidAmount == null ? BigDecimal.ZERO : paidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return getTotalDueAmountOrZero().subtract(getPaidAmountOrZero());
    }

    public boolean isPartiallyPaid() {
        return getTotalDueAmountOrZero().compareTo(getPaidAmountOrZero()) > 0;
    }

    public void reRegister() {
        setRegistrationTime(new Date());
        this.completed = false;
    }

    public void updateTotalDueAmount(final int chargeableAttempts) {
        Preconditions.checkArgument(chargeableAttempts >= 0, "number of attempts must not be negative");

        checkState(!this.completed, "Cannot change total due amount after participant is completed");

        final BigDecimal sum = calculatePaymentSum(chargeableAttempts);
        final int comparison = sum.compareTo(getPaidAmountOrZero());

        checkState(comparison >= 0, "Total due amount cannot be set lower than paid amount");

        this.totalDueAmount = sum;
    }

    public void setCompleted() {
        if (this.totalDueAmount == null) {
            this.totalDueAmount = BigDecimal.ZERO;
        }
        this.paidAmount = this.totalDueAmount;
        this.completed = true;
    }

    public void updatePaymentState(final int paidAttempts, final boolean completed) {
        Preconditions.checkArgument(paidAttempts >= 0, "number of attempts must not be negative");

        if (this.totalDueAmount == null) {
            this.totalDueAmount = BigDecimal.ZERO;
        }

        final BigDecimal sum = calculatePaymentSum(paidAttempts);
        final boolean isValid = BigDecimalComparison.of(sum).lte(this.totalDueAmount);

        checkState(isValid, "Paid amount cannot be set greater than total due amount");

        this.paidAmount = sum;
        this.completed = completed;
    }

    private static void checkState(final boolean condition, final String errorMessage) {
        if (!condition) {
            throw new IllegalShootingTestParticipantStateException(errorMessage);
        }
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "shooting_test_participant_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public ShootingTestEvent getShootingTestEvent() {
        return shootingTestEvent;
    }

    public void setShootingTestEvent(final ShootingTestEvent shootingTestEvent) {
        CriteriaUtils.updateInverseCollection(ShootingTestEvent_.participants, this, this.shootingTestEvent, shootingTestEvent);
        this.shootingTestEvent = shootingTestEvent;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(final Date registrationTime) {
        this.registrationTime = registrationTime;
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

    public BigDecimal getTotalDueAmount() {
        return totalDueAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public boolean isCompleted() {
        return completed;
    }

    Set<ShootingTestAttempt> getAttempts() {
        return attempts;
    }
}
