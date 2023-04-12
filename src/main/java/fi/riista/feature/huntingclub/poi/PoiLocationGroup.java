package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class PoiLocationGroup extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "poi_location_group_id";

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PoiIdAllocation poiIdAllocation;

    @NotNull
    @Convert(converter = PointOfInterestTypeConverter.class)
    @Column(nullable = false, length = 1)
    private PointOfInterestType type;

    @Min(1)
    @Column(nullable = false)
    private int visibleId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String description;

    public PoiLocationGroup() {
    }

    public PoiLocationGroup(@Nonnull final PoiIdAllocation poiIdAllocation,
                            @Nonnull final PointOfInterestType type,
                            @Nonnull final int visibleId) {
        this.poiIdAllocation = requireNonNull(poiIdAllocation);
        this.type = requireNonNull(type);
        this.visibleId = requireNonNull(visibleId);
    }

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

    public PoiIdAllocation getPoiIdAllocation() {
        return poiIdAllocation;
    }

    public void setPoiIdAllocation(final PoiIdAllocation clubPoi) {
        this.poiIdAllocation = clubPoi;
    }

    public PointOfInterestType getType() {
        return type;
    }

    public void setType(final PointOfInterestType type) {
        this.type = type;
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
}
