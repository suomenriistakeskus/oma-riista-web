package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.F;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class SrvaEvent extends LifecycleEntity<Long> {

    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 999;

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SrvaEventNameEnum eventName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SrvaEventTypeEnum eventType;

    @NotNull
    @Min(MIN_AMOUNT)
    @Max(MAX_AMOUNT)
    @Column(nullable = false)
    private Integer totalSpecimenAmount;

    @Column(columnDefinition = "text")
    private String otherMethodDescription;

    @Column(columnDefinition = "text")
    private String otherTypeDescription;

    @OneToMany(mappedBy = "event")
    private Set<SrvaMethod> methods = new HashSet<>();

    @Column
    private Integer personCount;

    // combined time used by all persons involved in hours
    @Column
    private Integer timeSpent;

    @Enumerated(EnumType.STRING)
    @Column
    private SrvaResultEnum eventResult;

    @Valid
    @NotNull
    @Embedded
    private GeoLocation geoLocation;

    // Geometry for GIS index. Updated using JPA lifecycle hooks. No accessor on purpose to avoid confusion.
    @NotNull
    @Column(nullable = false, columnDefinition = "Geometry")
    @Type(type = "jts_geometry")
    private Point geom;

    @NotNull
    @Column(nullable = false)
    private DateTime pointOfTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_species_id")
    private GameSpecies species;

    @Column(columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "event")
    private Set<SrvaSpecimen> specimens = new HashSet<>();

    @OneToMany(mappedBy = "srvaEvent")
    private Set<GameDiaryImage> images = new HashSet<>();

    @Column
    private Long mobileClientRefId;

    @Column(nullable = false, updatable = false)
    private boolean fromMobile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SrvaEventStateEnum state;

    @Column(columnDefinition = "text")
    private String otherSpeciesDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser approverAsUser;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person approverAsPerson;

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

    @AssertTrue
    protected boolean isExclusiveSpeciesOrOtherSpeciesDescription() {
        return species != null && otherSpeciesDescription == null ||
                species == null && otherSpeciesDescription != null;
    }

    @AssertTrue
    public boolean isGeolocationSourceDefined() {
        return geoLocation != null && geoLocation.getSource() != null;
    }

    @AssertTrue
    public boolean isApproverDefinedForOtherThanUnfinished() {
        return state == SrvaEventStateEnum.UNFINISHED && approverAsUser == null && approverAsPerson == null ||
                state != SrvaEventStateEnum.UNFINISHED && approverAsUser != null;
    }

    @AssertTrue
    public boolean isTypeMatchingName() {
        return eventType.matchesEventName(eventName);
    }

    @Override
    public String toString() {
        return "SrvaEvent{" +
                "id=" + id +
                ", eventName=" + eventName +
                ", eventType=" + eventType +
                ", totalSpecimenAmount=" + totalSpecimenAmount +
                ", otherMethodDescription='" + otherMethodDescription + '\'' +
                ", otherTypeDescription='" + otherTypeDescription + '\'' +
                ", personCount=" + personCount +
                ", timeSpent=" + timeSpent +
                ", eventResult=" + eventResult +
                ", geoLocation=" + geoLocation +
                ", pointOfTime=" + pointOfTime +
                ", author=" + author +
                ", species=" + species +
                ", description='" + description + '\'' +
                ", mobileClientRefId=" + mobileClientRefId +
                ", fromMobile=" + fromMobile +
                ", rhy=" + rhy +
                ", state=" + state +
                '}';
    }

    public List<SrvaSpecimen> getSortedSpecimens() {
        return F.sortedById(specimens);
    }

    public List<SrvaMethod> getSortedMethods() {
        return F.sortedById(methods);
    }

    public boolean isAccident() {
        return Objects.equals(getEventName(), SrvaEventNameEnum.ACCIDENT);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "srva_event_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public SrvaEventNameEnum getEventName() {
        return eventName;
    }

    public void setEventName(SrvaEventNameEnum eventName) {
        this.eventName = eventName;
    }

    public SrvaEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(SrvaEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public int getTotalSpecimenAmount() {
        return totalSpecimenAmount;
    }

    public void setTotalSpecimenAmount(Integer totalSpecimenAmount) {
        this.totalSpecimenAmount = totalSpecimenAmount;
    }

    public String getOtherMethodDescription() {
        return otherMethodDescription;
    }

    public void setOtherMethodDescription(String otherMethodDescription) {
        this.otherMethodDescription = otherMethodDescription;
    }

    public String getOtherTypeDescription() {
        return otherTypeDescription;
    }

    public void setOtherTypeDescription(String otherTypeDescription) {
        this.otherTypeDescription = otherTypeDescription;
    }

    public Integer getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Integer personCount) {
        this.personCount = personCount;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public SrvaResultEnum getEventResult() {
        return eventResult;
    }

    public void setEventResult(SrvaResultEnum eventResult) {
        this.eventResult = eventResult;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(GameSpecies species) {
        this.species = species;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    Set<SrvaMethod> getMethods() {
        return methods;
    }

    Set<SrvaSpecimen> getSpecimens() {
        return specimens;
    }

    Set<GameDiaryImage> getImages() {
        return images;
    }

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }

    public boolean isFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(boolean fromMobile) {
        this.fromMobile = fromMobile;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public SrvaEventStateEnum getState() {
        return state;
    }

    public void setState(SrvaEventStateEnum state) {
        this.state = state;
    }

    public String getOtherSpeciesDescription() {
        return otherSpeciesDescription;
    }

    public void setOtherSpeciesDescription(String otherSpeciesDescription) {
        this.otherSpeciesDescription = otherSpeciesDescription;
    }

    public SystemUser getApproverAsUser() {
        return approverAsUser;
    }

    public void setApproverAsUser(SystemUser approverAsUser) {
        this.approverAsUser = approverAsUser;
    }

    public Person getApproverAsPerson() {
        return approverAsPerson;
    }

    public void setApproverAsPerson(Person approverAsPerson) {
        this.approverAsPerson = approverAsPerson;
    }
}
