package fi.riista.feature.gamediary.srva.specimen;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.util.jpa.CriteriaUtils;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class SrvaSpecimen extends LifecycleEntity<Long> {
    public static final String ID_COLUMN_NAME = "srva_specimen_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "srva_event_id", nullable = false)
    private SrvaEvent event;

    @Column
    @Enumerated(EnumType.STRING)
    private GameGender gender;

    @Column
    @Enumerated(EnumType.STRING)
    private GameAge age;

    public SrvaSpecimen() {
    }

    public SrvaSpecimen(final SrvaEvent event) {
        setEvent(event);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(GameGender gender) {
        this.gender = gender;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(GameAge age) {
        this.age = age;
    }

    public SrvaEvent getEvent() {
        return event;
    }

    public void setEvent(SrvaEvent event) {
        CriteriaUtils.updateInverseCollection(SrvaEvent_.specimens, this, this.event, event);
        this.event = event;
    }

    public boolean isEqualBusinessFields(SrvaSpecimen other) {
        return other != null && other.getAge() == getAge() && other.getGender() == getGender();
    }

    @Override
    public String toString() {
        return "SrvaSpecimen{" +
                "age=" + age +
                ", gender=" + gender +
                '}';
    }
}
