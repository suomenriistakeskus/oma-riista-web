package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;

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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class ObservationBaseFields extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    private GameSpecies species;

    /**
     * Hirvenmetsästyksen yhteydessä tehty havainto
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required withinMooseHunting = Required.NO;

    /**
     * Peuran metsästyksen yhteydessä tehty havainto
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required withinDeerHunting = Required.NO;

    @Min(1)
    @Column(nullable = false)
    private int metadataVersion;

    @NotNull
    @Column(nullable = false, updatable = false)
    private DateTime creationTime;

    // Trigger-updated in production DB
    @OptimisticLock(excluded = true)
    @NotNull
    @Column(nullable = false)
    private DateTime modificationTime;

    // Public default constructor needed for tests.
    public ObservationBaseFields() {
    }

    public ObservationBaseFields(final GameSpecies species, final int metadataVersion) {
        this(species, metadataVersion, DateUtil.now());
    }

    public ObservationBaseFields(final GameSpecies species, final int metadataVersion, final DateTime creationTime) {
        setSpecies(species);
        this.metadataVersion = metadataVersion;
        this.creationTime = creationTime;
        this.modificationTime = creationTime;
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        CriteriaUtils.updateInverseCollection(GameSpecies_.observationBaseFields, this, this.species, species);
        this.species = species;
    }

    public Required getWithinMooseHunting() {
        return withinMooseHunting;
    }

    public void setWithinMooseHunting(final Required withinMooseHunting) {
        this.withinMooseHunting = withinMooseHunting;
    }

    public Required getWithinDeerHunting() {
        return withinDeerHunting;
    }

    public void setWithinDeerHunting(final Required withinDeerHunting) {
        this.withinDeerHunting = withinDeerHunting;
    }

    public int getMetadataVersion() {
        return metadataVersion;
    }

    public void setMetadataVersion(final int metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public DateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(final DateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

}
