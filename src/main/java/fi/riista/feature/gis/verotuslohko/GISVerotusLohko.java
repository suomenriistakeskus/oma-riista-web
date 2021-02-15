package fi.riista.feature.gis.verotuslohko;

import fi.riista.feature.common.entity.HasID;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Persistable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(AccessType.FIELD)
@Table(name = "verotus_lohko")
public class GISVerotusLohko implements Persistable<Integer>, HasID<Integer> {

    private Integer id;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String officialCode;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int huntingYear;

    @NotNull
    @Type(type = "jts_geometry")
    @Column(nullable = false, columnDefinition = "Geometry")
    private Geometry geom;

    @Column(nullable = false)
    private double areaSize;

    @Column(nullable = false)
    private double landSize;

    @Column(nullable = false)
    private double waterSize;

    @Column(nullable = false)
    private double stateSize;

    @Column(nullable = false)
    private double stateLandSize;

    @Column(nullable = false)
    private double stateWaterSize;

    @Column(nullable = false)
    private double privateSize;

    @Column(nullable = false)
    private double privateLandSize;

    @Column(nullable = false)
    private double privateWaterSize;

    @Override
    public boolean isNew() {
        return id != null;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof Persistable)) {
            return false;
        }

        final Persistable<?> thatPersistable = (Persistable<?>) that;

        return null != this.getId() && this.getId().equals(thatPersistable.getId());
    }

    @Override
    public int hashCode() {
        return null == getId() ? 0 : 17 + getId().hashCode() * 31;
    }

    // Accessors -->

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @Column(name = "gid", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(final Geometry geom) {
        this.geom = geom;
    }

    public double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final double areaSize) {
        this.areaSize = areaSize;
    }

    public double getLandSize() {
        return landSize;
    }

    public void setLandSize(final double landSize) {
        this.landSize = landSize;
    }

    public double getWaterSize() {
        return waterSize;
    }

    public void setWaterSize(final double waterSize) {
        this.waterSize = waterSize;
    }

    public double getStateSize() {
        return stateSize;
    }

    public void setStateSize(final double stateSize) {
        this.stateSize = stateSize;
    }

    public double getStateLandSize() {
        return stateLandSize;
    }

    public void setStateLandSize(final double stateLandSize) {
        this.stateLandSize = stateLandSize;
    }

    public double getStateWaterSize() {
        return stateWaterSize;
    }

    public void setStateWaterSize(final double stateWaterSize) {
        this.stateWaterSize = stateWaterSize;
    }

    public double getPrivateSize() {
        return privateSize;
    }

    public void setPrivateSize(final double privateSize) {
        this.privateSize = privateSize;
    }

    public double getPrivateLandSize() {
        return privateLandSize;
    }

    public void setPrivateLandSize(final double privateLandSize) {
        this.privateLandSize = privateLandSize;
    }

    public double getPrivateWaterSize() {
        return privateWaterSize;
    }

    public void setPrivateWaterSize(final double privateWaterSize) {
        this.privateWaterSize = privateWaterSize;
    }
}
