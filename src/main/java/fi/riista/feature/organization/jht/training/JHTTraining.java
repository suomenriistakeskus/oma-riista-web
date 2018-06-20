package fi.riista.feature.organization.jht.training;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.feature.common.entity.PersistableEnumConverter;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedEnum;
import fi.riista.validation.XssSafe;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "jht_training")
@Access(value = AccessType.FIELD)
public class JHTTraining extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "jht_training_id";

    // Person with current valid occupation RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA
    // do not require new trainings. To make them available for selection some artifical trainings
    // were populated to production database with training_location "Sähköinen" and fake date 1.1.1900
    private static final LocalDate ARTIFICAL_DATE = new LocalDate(1900, 1, 1);

    @Converter
    public static class TrainingTypeConverter implements PersistableEnumConverter<TrainingType> {
    }

    public enum TrainingType implements LocalisedEnum, PersistableEnum {
        LAHI("L"),
        SAHKOINEN("S");

        private final String databaseValue;

        TrainingType(final String databaseValue) {
            this.databaseValue = databaseValue;
        }

        @Override
        public String getDatabaseValue() {
            return databaseValue;
        }
    }

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

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String trainingLocation;

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

    public String getTrainingLocation() {
        return trainingLocation;
    }

    public void setTrainingLocation(final String trainingLocation) {
        this.trainingLocation = trainingLocation;
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

    public boolean isArtificialTraining() {
        return this.trainingType == TrainingType.LAHI && ARTIFICAL_DATE.equals(trainingDate);
    }
}
