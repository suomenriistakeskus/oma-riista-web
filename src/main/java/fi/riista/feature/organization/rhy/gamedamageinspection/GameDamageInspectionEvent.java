package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static fi.riista.util.DateUtil.today;

@Entity
@Access(AccessType.FIELD)
public class GameDamageInspectionEvent extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private GameSpecies gameSpecies;

    @Column
    @Size(min = 2, max = 255)
    private String inspectorName;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @NotNull
    @Column(nullable = false)
    public Date date;

    @NotNull
    @Column(nullable = false)
    private LocalTime beginTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private BigDecimal hourlyExpensesUnit;

    @Column
    private BigDecimal dailyAllowance;

    @OneToMany(mappedBy = "gameDamageInspectionEvent")
    private Set<GameDamageInspectionKmExpense> gameDamageInspectionKmExpenses = new HashSet<>();

    // Geometry for GIS index. Updated using JPA lifecycle hooks. No accessor on purpose to avoid confusion.
    @NotNull
    @Type(type = "jts_geometry")
    @Column(nullable = false, columnDefinition = "Geometry")
    private Point geom;

    @Column(nullable = false)
    private boolean expensesIncluded;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person inspector;

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
        final LocalDate today = today();
        final LocalDate eventDate = DateUtil.toLocalDateNullSafe(date);
        final LocalDate lastModificationDate = new LocalDate(today.getYear(), 3, 5);

        return eventDate.getYear() < today.minusDays(lastModificationDate.getDayOfYear()).getYear();
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "game_damage_inspection_event_id", nullable = false)
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

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(final GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(final String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public BigDecimal getHourlyExpensesUnit() {
        return hourlyExpensesUnit;
    }

    public void setHourlyExpensesUnit(final BigDecimal hourlyExpensesUnit) {
        this.hourlyExpensesUnit = hourlyExpensesUnit;
    }

    public BigDecimal getDailyAllowance() {
        return dailyAllowance;
    }

    public void setDailyAllowance(final BigDecimal dailyAllowance) {
        this.dailyAllowance = dailyAllowance;
    }

    public Set<GameDamageInspectionKmExpense> getGameDamageInspectionKmExpenses() {
        return gameDamageInspectionKmExpenses;
    }

    public boolean getExpensesIncluded() {
        return expensesIncluded;
    }

    public void setExpensesIncluded(final boolean expensesIncluded) {
        this.expensesIncluded = expensesIncluded;
    }

    public Person getInspector() {
        return inspector;
    }

    public void setInspector(final Person inspector) {
        this.inspector = inspector;
    }
}
