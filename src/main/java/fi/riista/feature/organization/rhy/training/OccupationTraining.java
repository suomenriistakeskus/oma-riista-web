package fi.riista.feature.organization.rhy.training;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.common.training.TrainingTypeConverter;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.validation.XssSafe;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
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
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class OccupationTraining extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "occupation_training_id";

    public static final boolean FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION_TRAINING = false;

    private Long id;

    @XssSafe
    @Size(max = 255)
    @Column
    private String externalId;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OccupationType occupationType;

    @NotNull
    @Column(nullable = false)
    private LocalDate trainingDate;

    @NotNull
    @Column(nullable = false, length = 1)
    @Convert(converter = TrainingTypeConverter.class)
    private TrainingType trainingType;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person person;

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(final TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(final LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

}
