package fi.riista.feature.gis.zone;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.hibernate.annotations.Type;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// Collection of selected property area boundaries
@Entity
@Table(name = "zone")
@Access(value = AccessType.FIELD)
public class GISZone extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "zone_id";

    public enum SourceType {
        LOCAL,
        EXTERNAL
    }

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @JoinColumn(unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private PersistentFileMetadata uploadFile;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Geometry geom;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Geometry excludedGeom;

    @Column(nullable = false)
    private double computedAreaSize;

    @Column(nullable = false)
    private double waterAreaSize;

    @Column
    private Double stateLandAreaSize;

    @Column
    private Double privateLandAreaSize;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "mh_hirvi_id")
    @CollectionTable(name = "zone_mh_hirvi", joinColumns = @JoinColumn(name = ID_COLUMN_NAME))
    private Set<Integer> metsahallitusHirvi = new HashSet<>();

    @Transient
    public double getLandAreaSize() {
        return computedAreaSize - waterAreaSize;
    }

    public boolean isGeometryEmpty() {
        return this.geom == null || this.geom.isEmpty();
    }

    public GISZone() {
        this(SourceType.LOCAL);
    }

    public GISZone(final SourceType sourceType) {
        setSourceType(sourceType);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(final SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public PersistentFileMetadata getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(final PersistentFileMetadata uploadFile) {
        this.uploadFile = uploadFile;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(final Geometry geom) {
        this.geom = geom;
    }

    public Geometry getExcludedGeom() {
        return excludedGeom;
    }

    public void setExcludedGeom(final Geometry excludedGeom) {
        this.excludedGeom = excludedGeom;
    }

    public Optional<Feature> getExcludedAsGeoJSON(final String excludedId) {
        return Optional.ofNullable(this.excludedGeom)
                .map(PolygonConversionUtil::javaToGeoJSON)
                .map(g -> {
                    final Feature excluded = new Feature();
                    excluded.setId(excludedId);
                    excluded.setGeometry(g);
                    return excluded;
                });
    }

    public double getComputedAreaSize() {
        return computedAreaSize;
    }

    public void setComputedAreaSize(final double computedAreaSize) {
        this.computedAreaSize = computedAreaSize;
    }

    public double getWaterAreaSize() {
        return waterAreaSize;
    }

    public void setWaterAreaSize(final double waterAreaSize) {
        this.waterAreaSize = waterAreaSize;
    }

    public Double getStateLandAreaSize() {
        return stateLandAreaSize;
    }

    public void setStateLandAreaSize(final Double stateLandAreaSize) {
        this.stateLandAreaSize = stateLandAreaSize;
    }

    public Double getPrivateLandAreaSize() {
        return privateLandAreaSize;
    }

    public void setPrivateLandAreaSize(final Double privateLandAreaSize) {
        this.privateLandAreaSize = privateLandAreaSize;
    }

    public Set<Integer> getMetsahallitusHirvi() {
        return metsahallitusHirvi;
    }

    public void setMetsahallitusHirvi(Set<Integer> metsahallitusHirvi) {
        this.metsahallitusHirvi = metsahallitusHirvi;
    }

}
