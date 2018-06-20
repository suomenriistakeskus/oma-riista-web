package fi.riista.feature.shootingtest;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.occupation.Occupation;
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
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
public class ShootingTestOfficial extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ShootingTestEvent shootingTestEvent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Occupation occupation;

    ShootingTestOfficial() {
    }

    public ShootingTestOfficial(final ShootingTestEvent shootingTestEvent, final Occupation occupation) {
        setShootingTestEvent(Objects.requireNonNull(shootingTestEvent));
        setOccupation(Objects.requireNonNull(occupation));
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "shooting_test_official_id", nullable = false)
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
        CriteriaUtils.updateInverseCollection(ShootingTestEvent_.officials, this, this.shootingTestEvent, shootingTestEvent);
        this.shootingTestEvent = shootingTestEvent;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(final Occupation occupation) {
        this.occupation = occupation;
    }
}
