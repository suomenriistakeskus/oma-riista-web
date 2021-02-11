package fi.riista.feature.permit.application.amendment;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.joda.time.DateTime;

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
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class AmendmentApplicationData extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "amendment_application_data_id";

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestPermit originalPermit;

    @ManyToOne(fetch = FetchType.LAZY)
    private Harvest nonEdibleHarvest;

    @Column
    private DateTime pointOfTime;

    @Column
    @Enumerated(EnumType.STRING)
    private GameAge age;

    @Column
    @Enumerated(EnumType.STRING)
    private GameGender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person shooter;

    @ManyToOne(fetch = FetchType.LAZY)
    private HuntingClub partner;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestPermitApplication getApplication() {
        return application;
    }

    public void setApplication(final HarvestPermitApplication application) {
        this.application = application;
    }

    public HarvestPermit getOriginalPermit() {
        return originalPermit;
    }

    public void setOriginalPermit(final HarvestPermit originalPermit) {
        this.originalPermit = originalPermit;
    }

    public Harvest getNonEdibleHarvest() {
        return nonEdibleHarvest;
    }

    public void setNonEdibleHarvest(final Harvest nonEdibleHarvest) {
        this.nonEdibleHarvest = nonEdibleHarvest;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(final GameAge age) {
        this.age = age;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    public Person getShooter() {
        return shooter;
    }

    public void setShooter(final Person shooter) {
        this.shooter = shooter;
    }

    public HuntingClub getPartner() {
        return partner;
    }

    public void setPartner(final HuntingClub partner) {
        this.partner = partner;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
