package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.locationtech.jts.geom.Point;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;

import static fi.riista.util.DateUtil.today;

@Entity
@Access(AccessType.FIELD)
public class HuntingControlEvent extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    @NotNull
    @Size(min = 2, max = 255)
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int inspectorCount;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HuntingControlCooperationType cooperationType;

    @Column(nullable = false)
    private boolean wolfTerritory;

    @NotNull
    @Column(nullable = false, columnDefinition = "text")
    private String inspectors;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @NotNull
    @Column(nullable = false)
    public LocalDate date;

    @NotNull
    @Column(nullable = false)
    private LocalTime beginTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int customers;

    @Column(nullable = false)
    private int proofOrders;

    @NotNull
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "huntingControlEvent")
    private List<HuntingControlAttachment> attachments = new LinkedList<>();

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

    @Transient
    public boolean isLockedAsPastStatistics() {
        return date.getYear() < today().minusDays(15).getYear();
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "hunting_control_event_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getInspectorCount() {
        return inspectorCount;
    }

    public void setInspectorCount(final int inspectorCount) {
        this.inspectorCount = inspectorCount;
    }

    public HuntingControlCooperationType getCooperationType() {
        return cooperationType;
    }

    public void setCooperationType(final HuntingControlCooperationType cooperationType) {
        this.cooperationType = cooperationType;
    }

    public boolean getWolfTerritory() {
        return wolfTerritory;
    }

    public void setWolfTerritory(final boolean wolfTerritory) {
        this.wolfTerritory = wolfTerritory;
    }

    public String getInspectors() {
        return inspectors;
    }

    public void setInspectors(final String inspectors) {
        this.inspectors = inspectors;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(final LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(final int customers) {
        this.customers = customers;
    }

    public int getProofOrders() {
        return proofOrders;
    }

    public void setProofOrders(final int proofOrders) {
        this.proofOrders = proofOrders;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<HuntingControlAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<HuntingControlAttachment> attachments) {
        this.attachments = attachments;
    }
}
