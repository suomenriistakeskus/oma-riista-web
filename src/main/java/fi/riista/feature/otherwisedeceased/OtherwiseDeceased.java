package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.Organisation;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.StringUtils.hasText;

@Entity
@Access(value = AccessType.FIELD)
public class OtherwiseDeceased extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "otherwise_deceased_id";

    // Factories

    public static OtherwiseDeceased create(@Nonnull final GameSpecies species,
                                           @Nonnull final GameAge age,
                                           @Nonnull final GameGender gender,
                                           final Double weight,
                                           @Nonnull final DateTime pointOfTime,
                                           final boolean noExactLocation,
                                           @Nonnull final GeoLocation geoLocation,
                                           @Nonnull final Municipality municipality,
                                           @Nonnull final Organisation rhy,
                                           @Nonnull final Organisation rka,
                                           @Nonnull final OtherwiseDeceasedCause cause,
                                           final String causeDescription,
                                           @Nonnull final OtherwiseDeceasedSource source,
                                           final String sourceDescription,
                                           final String description,
                                           final String additionalInfo) {
        requireNonNull(species);
        requireNonNull(age);
        requireNonNull(gender);
        requireNonNull(pointOfTime);
        requireNonNull(geoLocation);
        requireNonNull(municipality);
        requireNonNull(rhy);
        requireNonNull(rka);
        requireNonNull(cause);
        requireNonNull(source);

        final OtherwiseDeceased entity = new OtherwiseDeceased();
        entity.setSpecies(species);
        entity.setAge(age);
        entity.setGender(gender);
        entity.setWeight(weight);
        entity.setPointOfTime(pointOfTime);
        entity.setNoExactLocation(noExactLocation);
        entity.setGeoLocation(geoLocation);
        entity.setMunicipality(municipality);
        entity.setRhy(rhy);
        entity.setRka(rka);
        entity.setCause(cause);
        entity.setCauseDescription(causeDescription);
        entity.setSource(source);
        entity.setSourceDescription(sourceDescription);
        entity.setDescription(description);
        entity.setAdditionalInfo(additionalInfo);
        entity.setRejected(false);

        return entity;
    }

    // Attributes

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    private GameSpecies species;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameAge age;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameGender gender;

    @Column
    private Double weight;

    @Column
    private DateTime pointOfTime;

    @Column(nullable = false)
    private boolean noExactLocation;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @NotNull
    @JoinColumn(name = "municipality_official_code", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Municipality municipality;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisation rhy;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisation rka;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtherwiseDeceasedCause cause;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "cause_other")
    private String causeDescription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtherwiseDeceasedSource source;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(name = "source_other")
    private String sourceDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String description;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "otherwiseDeceased", cascade = CascadeType.ALL)
    private List<OtherwiseDeceasedAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "otherwiseDeceased", cascade = CascadeType.ALL)
    private List<OtherwiseDeceasedChange> changeHistory = new ArrayList<>();

    @Column(nullable = false)
    private boolean rejected;

    // Methods

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

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        this.species = species;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(final GameAge age) {
        this.age = age;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(final Double weight) {
        this.weight = weight;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public boolean getNoExactLocation() {
        return noExactLocation;
    }

    public void setNoExactLocation(final boolean noExactLocation) {
        this.noExactLocation = noExactLocation;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(final Municipality municipality) {
        this.municipality = municipality;
    }

    public Organisation getRhy() {
        return rhy;
    }

    public void setRhy(final Organisation rhy) {
        this.rhy = rhy;
    }

    public Organisation getRka() {
        return rka;
    }

    public void setRka(final Organisation rka) {
        this.rka = rka;
    }

    public OtherwiseDeceasedCause getCause() {
        return cause;
    }

    public void setCause(final OtherwiseDeceasedCause cause) {
        this.cause = cause;
    }

    public String getCauseDescription() {
        return causeDescription;
    }

    public void setCauseDescription(final String causeDescription) {
        this.causeDescription = causeDescription;
    }

    public OtherwiseDeceasedSource getSource() {
        return source;
    }

    public void setSource(final OtherwiseDeceasedSource source) {
        this.source = source;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(final String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    // Discourage using these getters of this entity.

    /* package */ List<OtherwiseDeceasedAttachment> getAttachments() {
        return attachments;
    }

    /* package */ List<OtherwiseDeceasedChange> getChangeHistory() {
        return changeHistory;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(final boolean rejected) {
        this.rejected = rejected;
    }

    @AssertTrue
    public boolean isCauseDescriptionSetWhenCauseOther() {
        return cause != OtherwiseDeceasedCause.OTHER || hasText(causeDescription);
    }
    @AssertTrue
    public boolean isSourceDescriptionSetWhenCauseOther() {
        return source != OtherwiseDeceasedSource.OTHER || hasText(sourceDescription);
    }
}
