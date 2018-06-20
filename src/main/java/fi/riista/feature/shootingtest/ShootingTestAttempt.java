package fi.riista.feature.shootingtest;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.validator.constraints.Range;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
public class ShootingTestAttempt extends LifecycleEntity<Long> {

    public static final ReadablePeriod SHOOTING_TEST_VALIDITY_PERIOD = Years.THREE;
    public static final int MAX_ATTEMPTS_PER_TYPE = 5;
    public static final BigDecimal ATTEMPT_PRICE = BigDecimal.valueOf(20.0);

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ShootingTestParticipant participant;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShootingTestType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShootingTestAttemptResult result;

    @Range(min = 0, max = 4)
    @Column
    private Integer hits;

    @Column(columnDefinition = "text")
    private String note;

    public ShootingTestAttempt() {
    }

    public ShootingTestAttempt(final ShootingTestParticipant participant,
                               final ShootingTestType type,
                               final ShootingTestAttemptResult result,
                               final Integer hits,
                               final String note) {

        setParticipant(Objects.requireNonNull(participant));
        this.type = Objects.requireNonNull(type);
        this.result = Objects.requireNonNull(result);
        this.hits = hits;
        this.note = note;
    }

    @AssertTrue
    public boolean isHitCountConsistentWithQualified() {
        return result != ShootingTestAttemptResult.QUALIFIED || hits != null && hits >= type.getNumberOfHitsToQualify();
    }

    @AssertTrue
    public boolean isHitCountConsistentWithUnqualified() {
        return result != ShootingTestAttemptResult.UNQUALIFIED || hits == null || hits < type.getNumberOfHitsToQualify();
    }

    public static BigDecimal calculatePaymentSum(final int chargeableAttempts) {
        return ATTEMPT_PRICE.multiply(BigDecimal.valueOf(chargeableAttempts));
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "shooting_test_attempt_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public ShootingTestParticipant getParticipant() {
        return participant;
    }

    public void setParticipant(final ShootingTestParticipant participant) {
        CriteriaUtils.updateInverseCollection(ShootingTestParticipant_.attempts, this, this.participant, participant);
        this.participant = participant;
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
}
