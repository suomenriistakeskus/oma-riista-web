package fi.riista.feature.gamediary.image;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.jpa.CriteriaUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class GameDiaryImage extends LifecycleEntity<Long> {

    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_id")
    private Harvest harvest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observation_id")
    private Observation observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "srva_event_id")
    private SrvaEvent srvaEvent;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_metadata_id", unique = true, nullable = false)
    private PersistentFileMetadata fileMetadata;

    protected GameDiaryImage() {
    }

    public GameDiaryImage(final PersistentFileMetadata fileMetadata) {
        super();

        setFileMetadata(fileMetadata);
    }

    public GameDiaryImage(final Harvest harvest, final PersistentFileMetadata fileMetadata) {
        this(fileMetadata);

        setHarvest(harvest);
    }

    public GameDiaryImage(final Observation observation, final PersistentFileMetadata fileMetadata) {
        this(fileMetadata);

        setObservation(observation);
    }

    public GameDiaryImage(final SrvaEvent event, final PersistentFileMetadata fileMetadata) {
        this(fileMetadata);

        setSrvaEvent(event);
    }

    @AssertTrue
    protected boolean isRelationToSrvaEventAndHarvestAndObservationExclusive() {
        return harvest == null && observation == null ||
                harvest == null && srvaEvent == null ||
                observation == null && srvaEvent == null;
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_diary_image_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Harvest getHarvest() {
        return harvest;
    }

    public void setHarvest(final Harvest harvest) {
        CriteriaUtils.updateInverseCollection(Harvest_.images, this, this.harvest, harvest);
        this.harvest = harvest;
    }

    public Observation getObservation() {
        return observation;
    }

    public void setObservation(final Observation observation) {
        CriteriaUtils.updateInverseCollection(Observation_.images, this, this.observation, observation);
        this.observation = observation;
    }

    public SrvaEvent getSrvaEvent() {
        return srvaEvent;
    }

    public void setSrvaEvent(final SrvaEvent event) {
        CriteriaUtils.updateInverseCollection(SrvaEvent_.images, this, this.srvaEvent, event);
        this.srvaEvent = event;
    }

    public PersistentFileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(final PersistentFileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

}
