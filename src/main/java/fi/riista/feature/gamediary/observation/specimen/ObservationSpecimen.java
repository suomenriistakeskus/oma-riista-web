package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.util.jpa.CriteriaUtils;

import javax.annotation.Nonnull;
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
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
public class ObservationSpecimen extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_observation_id", nullable = false)
    private Observation observation;

    @Column
    @Enumerated(EnumType.STRING)
    private GameGender gender;

    @Column
    @Enumerated(EnumType.STRING)
    private ObservedGameAge age;

    @Column
    @Enumerated(EnumType.STRING)
    private ObservedGameState state;

    @Column
    @Enumerated(EnumType.STRING)
    private GameMarking marking;

    // Default constructor for Hibernate
    protected ObservationSpecimen() {
    }

    public ObservationSpecimen(final Observation observation) {
        setObservation(observation);
    }

    public boolean hasEqualBusinessFields(@Nonnull final ObservationSpecimen other) {
        Objects.requireNonNull(other);

        return getGender() == other.getGender() &&
                getAge() == other.getAge() &&
                getState() == other.getState() &&
                getMarking() == other.getMarking();
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "observation_specimen_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Observation getObservation() {
        return observation;
    }

    public void setObservation(final Observation observation) {
        CriteriaUtils.updateInverseCollection(Observation_.specimens, this, this.observation, observation);
        this.observation = observation;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    public ObservedGameAge getAge() {
        return age;
    }

    public void setAge(final ObservedGameAge age) {
        this.age = age;
    }

    public ObservedGameState getState() {
        return state;
    }

    public void setState(final ObservedGameState state) {
        this.state = state;
    }

    public GameMarking getMarking() {
        return marking;
    }

    public void setMarking(final GameMarking marking) {
        this.marking = marking;
    }

}
