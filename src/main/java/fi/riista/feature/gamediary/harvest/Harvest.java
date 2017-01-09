package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.Person_;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.validator.constraints.Range;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

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
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class Harvest extends GameDiaryEntry {

    public enum StateAcceptedToHarvestPermit {
        PROPOSED,
        ACCEPTED,
        REJECTED
    }

    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 999;

    private Long id;

    @Range(min = MIN_AMOUNT, max = MAX_AMOUNT)
    @Column(nullable = false)
    private int amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person actualShooter;

    @Column(nullable = true, insertable = true, updatable = false)
    private Boolean fromMobile;

    // Opaque identifier reference to MH geometry
    @Column(name = "mh_hirvi_id")
    private Integer metsahallitusHirviAlueId;

    // Opaque identifier reference to MH geometry
    @Column(name = "mh_pienriista_id")
    private Integer metsahallitusPienriistaAlueId;

    @Pattern(regexp = "^\\d{0,3}$")
    @Column(name = "municipality_code", nullable = true, length = 3)
    private String municipalityCode;

    @Enumerated(EnumType.STRING)
    @Column
    private HarvestLukeStatus lukeStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestPermit harvestPermit;

    @Enumerated(EnumType.STRING)
    @Column
    private StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit;

    @OneToMany(mappedBy = "harvest")
    private Set<HarvestSpecimen> specimens = new HashSet<>();

    @OneToMany(mappedBy = "harvest")
    private Set<GameDiaryImage> images = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestReport harvestReport;

    @Column(nullable = false)
    private boolean harvestReportRequired;

    // if harvest report is required and not done, this contains latest time when email reminder is sent
    @Column
    private DateTime emailReminderSentTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestSeason harvestSeason;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestQuota harvestQuota;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestReportFields harvestReportFields;

    @Embedded
    private PropertyIdentifier propertyIdentifier;

    @Enumerated(EnumType.STRING)
    @Column
    private HuntingAreaType huntingAreaType;

    @OneToMany(mappedBy = "harvest")
    private Set<HarvestRejection> groupRejections = new HashSet<>();

    @Size(max = 255)
    @Column
    private String huntingParty;

    @Min(0)
    @Max(99999999)
    @Column(precision = 10, scale = 2)
    private Double huntingAreaSize;

    @Enumerated(EnumType.STRING)
    @Column
    private HuntingMethod huntingMethod;

    @Column
    private Boolean reportedWithPhoneCall;

    @Embedded
    private PermittedMethod permittedMethod;

    @Transient
    private HuntingClub huntingClub;

    public Harvest() {
        super();
        this.amount = 0;
    }

    public Harvest(
            final Person author,
            final GeoLocation geoLocation,
            final LocalDateTime pointOfTime,
            final GameSpecies species,
            final int amount) {

        super(geoLocation, pointOfTime, species, author);

        setActualShooter(author);
        this.amount = amount;
    }

    @Override
    public GameDiaryEntryType getType() {
        return GameDiaryEntryType.HARVEST;
    }

    @Override
    public Person getActor() {
        return getActualShooter();
    }

    @Override
    public void setActor(Person person) {
        setActualShooter(person);
    }

    @Override
    public void updateGeoLocation(final GeoLocation geoLocation, final GISQueryService gisQueryService) {
        // Code organized so that queries are first and mutations last in order
        // to reduce intermediary flushes to database. Implicit flushes may
        // occur before query execution if entities appearing in query are in
        // dirty state. Unnecessary flushes may increase entity revision by
        // many integer "units" within one database transaction.

//        Integer metsahallitusHirviAlueId = null;
//        Integer metsahallitusPienriistaAlueId = null;
        String municipalityCode = null;

        if (geoLocation != null) {
//            metsahallitusHirviAlueId = gisQueryService.findMetsahallitusHirviAlueId(geoLocation);
//            metsahallitusPienriistaAlueId = gisQueryService.findMetsahallitusPienriistaAlueId(geoLocation);

            final Municipality municipality = gisQueryService.findMunicipality(geoLocation);
            municipalityCode = municipality == null ? null : municipality.getOfficialCode();
        }

        super.updateGeoLocation(geoLocation, gisQueryService);

//        setMetsahallitusHirviAlueId(metsahallitusHirviAlueId);
//        setMetsahallitusPienriistaAlueId(metsahallitusPienriistaAlueId);
        setMunicipalityCode(municipalityCode);
    }

    public boolean isHarvestReportDone() {
        return harvestReport != null && harvestReport.getState() != HarvestReport.State.DELETED;
    }

    public HarvestReport getUndeletedHarvestReportOrNull() {
        return isHarvestReportDone() ? harvestReport : null;
    }

    public boolean canModeratorDelete() {
        return harvestReport == null
                && stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.REJECTED
                && description == null
                && images.isEmpty();
    }

    public List<HarvestSpecimen> getSortedSpecimens() {
        return F.sortedById(specimens);
    }

    @Override
    protected void updateAuthorInverseCollection(final Person newAuthor) {
        CriteriaUtils.updateInverseCollection(Person_.authoredHarvests, this, getAuthor(), newAuthor);
    }

    @Override
    protected void updateHuntingDayOfGroupInverseCollection(final GroupHuntingDay newHuntingDay) {
        CriteriaUtils.updateInverseCollection(GroupHuntingDay_.harvests, this, this.huntingDayOfGroup, newHuntingDay);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public Person getActualShooter() {
        return actualShooter;
    }

    public void setActualShooter(final Person actualShooter) {
        CriteriaUtils.updateInverseCollection(Person_.huntedHarvests, this, this.actualShooter, actualShooter);
        this.actualShooter = actualShooter;
    }

    public Boolean getFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(final Boolean fromMobile) {
        this.fromMobile = fromMobile;
    }

    public Integer getMetsahallitusHirviAlueId() {
        return metsahallitusHirviAlueId;
    }

    public void setMetsahallitusHirviAlueId(final Integer metsahallitusHirviAlueId) {
        this.metsahallitusHirviAlueId = metsahallitusHirviAlueId;
    }

    public Integer getMetsahallitusPienriistaAlueId() {
        return metsahallitusPienriistaAlueId;
    }

    public void setMetsahallitusPienriistaAlueId(final Integer metsahallitusPienriistaAlueId) {
        this.metsahallitusPienriistaAlueId = metsahallitusPienriistaAlueId;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public HarvestLukeStatus getLukeStatus() {
        return lukeStatus;
    }

    public void setLukeStatus(final HarvestLukeStatus lukeStatus) {
        this.lukeStatus = lukeStatus;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    public void setHarvestPermit(final HarvestPermit harvestPermit) {
        this.harvestPermit = harvestPermit;
    }

    public StateAcceptedToHarvestPermit getStateAcceptedToHarvestPermit() {
        return stateAcceptedToHarvestPermit;
    }

    public void setStateAcceptedToHarvestPermit(final StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {
        this.stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit;
    }

    Set<HarvestSpecimen> getSpecimens() {
        return specimens;
    }

    public Set<GameDiaryImage> getImages() {
        return images;
    }

    public HarvestReport getHarvestReport() {
        return harvestReport;
    }

    public void setHarvestReport(final HarvestReport harvestReport) {
        this.harvestReport = harvestReport;
    }

    public boolean isHarvestReportRequired() {
        return harvestReportRequired;
    }

    public void setHarvestReportRequired(final boolean harvestReportRequired) {
        this.harvestReportRequired = harvestReportRequired;
    }

    public DateTime getEmailReminderSentTime() {
        return emailReminderSentTime;
    }

    public void setEmailReminderSentTime(final DateTime emailReminderSentTime) {
        this.emailReminderSentTime = emailReminderSentTime;
    }

    public HarvestSeason getHarvestSeason() {
        return harvestSeason;
    }

    public void setHarvestSeason(final HarvestSeason harvestSeason) {
        this.harvestSeason = harvestSeason;
    }

    public HarvestQuota getHarvestQuota() {
        return harvestQuota;
    }

    public void setHarvestQuota(final HarvestQuota harvestQuota) {
        this.harvestQuota = harvestQuota;
    }

    public HarvestReportFields getHarvestReportFields() {
        return harvestReportFields;
    }

    public void setHarvestReportFields(final HarvestReportFields harvestReportFields) {
        this.harvestReportFields = harvestReportFields;
    }

    public PropertyIdentifier getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public void setPropertyIdentifier(final PropertyIdentifier propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    public void setPropertyIdentifier(final String value) {
        this.propertyIdentifier = value != null
                ? PropertyIdentifier.create(value)
                : null;
    }

    public HuntingAreaType getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(final HuntingAreaType huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    // HarvestRejection collection getter exposed in package-private scope only for property introspection.
    Set<HarvestRejection> getGroupRejections() {
        return groupRejections;
    }

    public String getHuntingParty() {
        return huntingParty;
    }

    public void setHuntingParty(final String huntingParty) {
        this.huntingParty = huntingParty;
    }

    public Double getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(final Double huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    public HuntingMethod getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(final HuntingMethod huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Boolean getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }

    public void setReportedWithPhoneCall(final Boolean reportedWithPhoneCall) {
        this.reportedWithPhoneCall = reportedWithPhoneCall;
    }

    public PermittedMethod getPermittedMethod() {
        return permittedMethod;
    }

    public void setPermittedMethod(final PermittedMethod permittedMethod) {
        this.permittedMethod = permittedMethod;
    }

    public HuntingClub getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(HuntingClub huntingClub) {
        this.huntingClub = huntingClub;
    }
}
