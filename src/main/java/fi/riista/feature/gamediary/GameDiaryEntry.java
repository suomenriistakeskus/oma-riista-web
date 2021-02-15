package fi.riista.feature.gamediary;

import com.google.common.base.Preconditions;
import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.locationtech.jts.geom.Point;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@MappedSuperclass
@Access(value = AccessType.FIELD)
public abstract class GameDiaryEntry extends LifecycleEntity<Long> {

    public static final boolean FOREIGN_PERSON_ELIGIBLE_AS_AUTHOR = false;
    public static final boolean FOREIGN_PERSON_ELIGIBLE_AS_ACTOR = true;

    @Valid
    @NotNull
    @Embedded
    protected GeoLocation geoLocation;

    // Geometry for GIS index. Updated using JPA lifecycle hooks. No accessor on purpose to avoid confusion.
    @NotNull
    @Type(type = "jts_geometry")
    @Column(nullable = false, columnDefinition = "Geometry")
    private Point geom;

    @NotNull
    @Column(nullable = false)
    protected DateTime pointOfTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    protected GameSpecies species;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    protected Person author;

    @Column(columnDefinition = "text")
    protected String description;

    @Column
    protected Long mobileClientRefId;

    // Indicates that this GameDiaryEntry has been modified by a moderator
    @Column(nullable = false)
    private boolean moderatorOverride;

    @ManyToOne(fetch = FetchType.LAZY)
    protected Riistanhoitoyhdistys rhy;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "group_hunting_day_id")
    protected GroupHuntingDay huntingDayOfGroup;

    // Person who linked this entry to hunting day. If acceptor is moderator, this is null.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    protected Person approverToHuntingDay;

    // Point of time when entry was linked to hunting day
    @Column
    protected Date pointOfTimeApprovedToHuntingDay;

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

    public GameDiaryEntry() {
        super();
    }

    public GameDiaryEntry(final GeoLocation geoLocation,
                          final LocalDateTime pointOfTime,
                          final GameSpecies species,
                          final Person author) {

        this.geoLocation = geoLocation;
        this.pointOfTime = pointOfTime.toDateTime(Constants.DEFAULT_TIMEZONE);
        this.species = species;

        setAuthor(author);
    }

    public abstract GameDiaryEntryType getType();

    public LocalDate getPointOfTimeAsLocalDate() {
        return getPointOfTime().toLocalDate();
    }

    public abstract Person getActor();

    public abstract void setActor(Person person);

    @AssertTrue
    public boolean isGeolocationSourceDefined() {
        return geoLocation != null && geoLocation.getSource() != null;
    }

    protected abstract void updateAuthorInverseCollection(Person newAuthor);

    protected abstract void updateHuntingDayOfGroupInverseCollection(GroupHuntingDay newHuntingDay);

    public boolean isAuthor(final Person person) {
        final Person author = getAuthor();
        return person != null && author != null && Objects.equals(person.getId(), author.getId());
    }

    public boolean isActor(final Person person) {
        final Person actor = getActor();
        return person != null && actor != null && Objects.equals(person.getId(), actor.getId());
    }

    public boolean isAuthorOrActor(final SystemUser user) {
        return user != null && user.getPerson() != null && isAuthorOrActor(user.getPerson());
    }

    public boolean isAuthorOrActor(final Person person) {
        return isAuthor(person) || isActor(person);
    }

    public Optional<HuntingClubGroup> getHuntingClubGroup() {
        return Optional.ofNullable(huntingDayOfGroup).map(GroupHuntingDay::getGroup);
    }

    @AssertTrue
    public boolean isNullabilityConsistentBetweenApproverAndHuntingDay() {
        return huntingDayOfGroup != null || approverToHuntingDay == null;
    }

    // Accessors -->

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation value) {
        this.geoLocation = value;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        this.species = species;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(final Person author) {
        updateAuthorInverseCollection(author);
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(final Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }

    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    public void setModeratorOverride(final boolean moderatorOverride) {
        this.moderatorOverride = moderatorOverride;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public GroupHuntingDay getHuntingDayOfGroup() {
        return huntingDayOfGroup;
    }

    public void updateHuntingDayOfGroup(final GroupHuntingDay huntingDayOfGroup, final Person approver) {
        Preconditions.checkNotNull(huntingDayOfGroup, "huntingDayOfGroup must be non null");
        this.approverToHuntingDay = approver;
        this.pointOfTimeApprovedToHuntingDay = DateUtil.now().toDate();
        updateHuntingDayOfGroupInverseCollection(huntingDayOfGroup);
        this.huntingDayOfGroup = huntingDayOfGroup;
    }

    public void unsetHuntingDayOfGroup() {
        this.approverToHuntingDay = null;
        this.pointOfTimeApprovedToHuntingDay = null;
        updateHuntingDayOfGroupInverseCollection(null);
        this.huntingDayOfGroup = null;
    }

    public Person getApproverToHuntingDay() {
        return approverToHuntingDay;
    }

    public Date getPointOfTimeApprovedToHuntingDay() {
        return pointOfTimeApprovedToHuntingDay;
    }
}
