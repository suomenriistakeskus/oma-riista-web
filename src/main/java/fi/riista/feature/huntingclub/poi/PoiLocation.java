package fi.riista.feature.huntingclub.poi;


import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class PoiLocation extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "poi_location_id";

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PoiLocationGroup poi;

    @Min(1)
    @Column(nullable = false)
    private int visibleId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String description;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public PoiLocationGroup getPoi() {
        return poi;
    }

    public void setPoi(final PoiLocationGroup poi) {
        this.poi = poi;
    }

    public int getVisibleId() {
        return visibleId;
    }

    public void setVisibleId(final int visibleId) {
        this.visibleId = visibleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
