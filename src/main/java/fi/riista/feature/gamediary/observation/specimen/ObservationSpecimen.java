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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MAX_PAW_LENGTH_OF_LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MAX_PAW_WIDTH_OF_BEAR;
import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MIN_PAW_LENGTH_OF_LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MIN_PAW_WIDTH_OF_LARGE_CARNIVORES;
import static fi.riista.util.NumberUtils.isInRange;

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

    /**
     * Tassun leveys (cm)
     */
    @Column
    private Double widthOfPaw;

    /**
     * Tassun pituus (cm)
     */
    @Column
    private Double lengthOfPaw;

    // Default constructor for Hibernate
    protected ObservationSpecimen() {
    }

    public ObservationSpecimen(@Nonnull final Observation observation) {
        setObservation(Objects.requireNonNull(observation));
    }

    @AssertTrue
    public boolean isWidthOfPawInValidRange() {
        return widthOfPaw == null || isInRange(widthOfPaw, MIN_PAW_WIDTH_OF_LARGE_CARNIVORES, MAX_PAW_WIDTH_OF_BEAR);
    }

    @AssertTrue
    public boolean isLengthOfPawInValidRange() {
        return lengthOfPaw == null
                || isInRange(lengthOfPaw, MIN_PAW_LENGTH_OF_LARGE_CARNIVORES, MAX_PAW_LENGTH_OF_LARGE_CARNIVORES);
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

    public Double getWidthOfPaw() {
        return widthOfPaw;
    }

    public void setWidthOfPaw(final Double widthOfPaw) {
        this.widthOfPaw = widthOfPaw;
    }

    public Double getLengthOfPaw() {
        return lengthOfPaw;
    }

    public void setLengthOfPaw(final Double lengthOfPaw) {
        this.lengthOfPaw = lengthOfPaw;
    }
}
