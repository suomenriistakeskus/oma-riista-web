package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsService;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.locationtech.jts.geom.Point;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static fi.riista.util.DateUtil.today;

@Entity
@Access(AccessType.FIELD)
public class HuntingControlEvent extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    // Replaces title field
    @Enumerated(EnumType.STRING)
    private HuntingControlEventType eventType;

    @Enumerated(EnumType.STRING)
    private HuntingControlEventStatus status;

    @ManyToMany
    @JoinTable(name = "hunting_control_event_inspector",
            joinColumns = {@JoinColumn(name = "hunting_control_event_id", referencedColumnName = "hunting_control_event_id")},
            inverseJoinColumns = {@JoinColumn(name = "person_id", referencedColumnName = "person_id")})
    private Set<Person> inspectors = new HashSet<>();

    @Deprecated
    @Size(min = 2, max = 255)
    @Column
    private String title;

    @Column(nullable = false)
    private int inspectorCount;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "hunting_control_cooperation", joinColumns = @JoinColumn(name = "hunting_control_event_id"))
    @Column(name = "type")
    private Set<HuntingControlCooperationType> cooperationTypes = new HashSet<>();

    @Column(nullable = false)
    private boolean wolfTerritory;

    @Column(columnDefinition = "text")
    private String otherParticipants;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @Column(columnDefinition = "text")
    private String locationDescription;

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

    @Column(columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "huntingControlEvent")
    private List<HuntingControlAttachment> attachments = new LinkedList<>();

    @OneToMany(mappedBy = "huntingControlEvent")
    private List<HuntingControlEventChange> changeHistory = new ArrayList<>();

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
        return AnnualStatisticsService.hasDeadlinePassed(date);
    }

    // For preventing duplicate entries from mobiles
    @Column
    private Long mobileClientRefId;

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

    public HuntingControlEventType getEventType() {
        return eventType;
    }

    public void setEventType(final HuntingControlEventType eventType) {
        this.eventType = eventType;
    }

    public HuntingControlEventStatus getStatus() {
        return status;
    }

    public void setStatus(final HuntingControlEventStatus status) {
        this.status = status;
    }

    public Set<Person> getInspectors() {
        return inspectors;
    }

    public void setInspectors(final Set<Person> inspectors) {
        this.inspectors = inspectors;
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

    public Set<HuntingControlCooperationType> getCooperationTypes() {
        return cooperationTypes;
    }

    public void setCooperationTypes(final Set<HuntingControlCooperationType> cooperationTypes) {
        this.cooperationTypes = cooperationTypes;
    }

    public boolean getWolfTerritory() {
        return wolfTerritory;
    }

    public void setWolfTerritory(final boolean wolfTerritory) {
        this.wolfTerritory = wolfTerritory;
    }

    public String getOtherParticipants() {
        return otherParticipants;
    }

    public void setOtherParticipants(final String otherParticipants) {
        this.otherParticipants = otherParticipants;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(final String locationDescription) {
        this.locationDescription = locationDescription;
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

    public List<HuntingControlEventChange> getChangeHistory() {
        return changeHistory;
    }

    public void setChangeHistory(final List<HuntingControlEventChange> changeHistory) {
        this.changeHistory = changeHistory;
    }

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(final Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }
}
