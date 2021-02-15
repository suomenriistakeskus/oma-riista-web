package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.GeoLocation;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class PermitUsageLocation extends BaseEntity<Long> {
    public static final String ID_COLUMN_NAME = "permit_usage_location_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitUsage permitUsage;

    @Valid
    @NotNull
    @Embedded
    private GeoLocation geoLocation;

    // Geometry for GIS index. Updated using JPA lifecycle hooks. No accessor on purpose to avoid confusion.
    @NotNull
    @Type(type = "jts_geometry")
    @Column(nullable = false, columnDefinition = "Geometry")
    private Point geom;

    @PrePersist
    @PreUpdate
    private void updatePointGeometry() {
        if (this.geoLocation == null) {
            this.geom = null;

        } else {
            final Point newGeom = this.geoLocation.toPointGeometry();

            if (this.geom == null || !newGeom.equalsExact(this.geom)) {
                // Skip update to prevent increasing consistency_version
                this.geom = newGeom;
            }
        }
    }

    public PermitUsageLocation() {
    }

    public PermitUsageLocation(final @NotNull PermitUsage permitUsage, final @Valid @NotNull GeoLocation geoLocation) {
        this.permitUsage = permitUsage;
        this.geoLocation = geoLocation;
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
    public void setId(final Long id) {
        this.id = id;
    }

    public PermitUsage getPermitUsage() {
        return permitUsage;
    }

    public void setPermitUsage(final PermitUsage permitUsage) {
        this.permitUsage = permitUsage;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
