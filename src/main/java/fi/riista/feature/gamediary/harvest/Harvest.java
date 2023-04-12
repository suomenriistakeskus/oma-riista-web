package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.report.HasHarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.Person_;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
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
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@Entity
@Access(value = AccessType.FIELD)
public class Harvest extends GameDiaryEntry implements HasHarvestReportState {

    /**
     * Harvest report is required if harvest report fields exist for that
     * species, and harvest date is REQUIRED_SINCE and after REQUIRED_SINCE.
     * Before REQUIRED_SINCE harvest reports are never required.
     */
    public static final LocalDate REPORT_REQUIRED_SINCE = new LocalDate(2014, 8, 1);

    public enum StateAcceptedToHarvestPermit {
        PROPOSED,
        ACCEPTED,
        REJECTED
    }

    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 9999;

    private Long id;

    @Min(MIN_AMOUNT)
    @Max(MAX_AMOUNT)
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

    // Permit reported for non-mooselike harvest
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestPermit harvestPermit;

    // Has contact person accepted this harvest permit?
    @Enumerated(EnumType.STRING)
    @Column
    private StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit;

    @OneToMany(mappedBy = "harvest")
    private Set<HarvestSpecimen> specimens = new HashSet<>();

    @OneToMany(mappedBy = "harvest")
    private Set<GameDiaryImage> images = new HashSet<>();

    // Processing status for moderator
    @Column
    @Enumerated(EnumType.STRING)
    private HarvestReportState harvestReportState;

    @JoinColumn(name = "harvest_report_author_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Person harvestReportAuthor;

    // Timestamp used to determine if harvest report was done late.
    @Column
    private DateTime harvestReportDate;

    // Memo editable only for moderator during harvest report processing
    @Column(columnDefinition = "text")
    private String harvestReportMemo;

    // TODO: Remove when user can no longer create harvest without report when required (mobile API)
    @Column(nullable = false)
    private boolean harvestReportRequired;

    @OneToMany(mappedBy = "harvest", cascade = CascadeType.ALL)
    private List<HarvestChangeHistory> changeHistory = new ArrayList<>();

    // if harvest report is required and not done, this contains latest time when email reminder is sent
    @Column
    private DateTime emailReminderSentTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestSeason harvestSeason;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HarvestQuota harvestQuota;

    @Valid
    @Embedded
    private PropertyIdentifier propertyIdentifier;

    // Pyyntialueen tyyppi
    @Enumerated(EnumType.STRING)
    @Column
    private HuntingAreaType huntingAreaType;

    @OneToMany(mappedBy = "harvest")
    private Set<HarvestRejection> groupRejections = new HashSet<>();

    @Size(max = 255)
    @Column
    private String huntingParty;

    @Column
    private Integer subSpeciesCode;

    @Min(0)
    @Max(99999999)
    @Column(precision = 10, scale = 2)
    private Double huntingAreaSize;

    // Hallin saalistustapa
    @Enumerated(EnumType.STRING)
    @Column
    private HuntingMethod huntingMethod;

    // Onko ilmoitettu myös saalispuhelimeen?
    @Column
    private Boolean reportedWithPhoneCall;

    @Column
    private Boolean feedingPlace;

    @Valid
    @Embedded
    private PermittedMethod permittedMethod;

    @OneToMany(mappedBy = "harvest", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<HarvestRegistryItem> harvestRegistryItem = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column
    private DeerHuntingType deerHuntingType;

    @Size(max = 255)
    @Column
    private String deerHuntingOtherTypeDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private HuntingClub huntingClub;

    @Transient
    private HuntingClub huntingClubForStatistics;

    public Harvest() {
        super();
        this.amount = 0;
    }

    public Harvest(final Person author,
                   final GeoLocation geoLocation,
                   final LocalDateTime pointOfTime,
                   final GameSpecies species,
                   final int amount) {

        super(geoLocation, pointOfTime, species, author);

        setActualShooter(author);
        this.amount = amount;
    }

    @AssertTrue
    public boolean isSeasonHarvestValid() {
        return this.harvestSeason == null && this.harvestQuota == null
                || (this.harvestSeason != null
                && this.harvestReportState != null
                && this.harvestPermit == null
                && this.huntingDayOfGroup == null);
    }

    @AssertTrue
    public boolean isHarvestPermitAbsentWithGroupHuntingDay() {
        return this.harvestPermit == null || this.huntingDayOfGroup == null;
    }

    @AssertTrue
    public boolean isHarvestPermitAcceptedStatePresentWithPermit() {
        return F.allNull(this.harvestPermit, this.stateAcceptedToHarvestPermit) ||
                F.allNotNull(this.harvestPermit, this.stateAcceptedToHarvestPermit);
    }

    @AssertTrue
    public boolean isAcceptedToPermitWithHarvestReport() {
        return this.harvestPermit == null || this.harvestReportState == null ||
                stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.ACCEPTED;
    }

    @AssertTrue
    public boolean isHarvestReportFieldsConsistent() {
        return F.allNull(this.harvestReportAuthor, this.harvestReportState, this.harvestReportDate) ||
                F.allNotNull(this.harvestReportAuthor, this.harvestReportState, this.harvestReportDate);
    }

    @AssertTrue
    public boolean isDeerHuntingDecriptionSetOnlyForOtherType() {
        return StringUtils.isEmpty(this.deerHuntingOtherTypeDescription) || this.deerHuntingType == DeerHuntingType.OTHER;
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

    public boolean isAcceptedToHarvestPermit() {
        return stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.ACCEPTED;
    }

    public boolean isProposedToHarvestPermit() {
        return stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.PROPOSED;
    }

    public boolean isRejectedFromHarvestPermit() {
        return stateAcceptedToHarvestPermit == StateAcceptedToHarvestPermit.REJECTED;
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

    public Boolean getTaigaBeanGoose() {
        if (isTaigaBeanGoose()) {
            return Boolean.TRUE;
        } else if (isTundraBeanGoose()) {
            return Boolean.FALSE;
        }
        return null;
    }

    public void setTaigaBeanGoose(Boolean value) {
        if (Boolean.TRUE.equals(value)) {
            setSubSpeciesCode(GameSpecies.OFFICIAL_CODE_TAIGA_BEAN_GOOSE);
        } else if (Boolean.FALSE.equals(value)) {
            setSubSpeciesCode(GameSpecies.OFFICIAL_CODE_TUNDRA_BEAN_GOOSE);
        } else {
            setSubSpeciesCode(null);
        }
    }

    public boolean isTaigaBeanGoose() {
        return this.subSpeciesCode != null && this.subSpeciesCode == GameSpecies.OFFICIAL_CODE_TAIGA_BEAN_GOOSE;
    }

    public boolean isTundraBeanGoose() {
        return this.subSpeciesCode != null && this.subSpeciesCode == GameSpecies.OFFICIAL_CODE_TUNDRA_BEAN_GOOSE;
    }

    @Nullable
    public HarvestReportState findHarvestReportStateAt(DateTime timestamp) {
        HarvestChangeHistory lowerBound = HarvestChangeHistory.withCreationTime(timestamp);

        // Find elements which have strictly lower creationTime
        SortedSet<HarvestChangeHistory> sorted = F.stream(changeHistory)
                .collect(toCollection(() -> new TreeSet<>(comparing(HarvestChangeHistory::getPointOfTime))));
        SortedSet<HarvestChangeHistory> headSet = sorted.headSet(lowerBound);

        if (headSet.isEmpty()) {
            // State is unknown at given timestamp
            return null;
        }

        // Result is the greatest item lower than given timestamp
        return headSet.last().getHarvestReportState();
    }

    public HarvestReportingType resolveReportingType() {
        if (huntingDayOfGroup != null) {
            return HarvestReportingType.HUNTING_DAY;
        }
        if (harvestSeason != null) {
            return HarvestReportingType.SEASON;
        }
        if (harvestPermit != null) {
            return HarvestReportingType.PERMIT;
        }
        return HarvestReportingType.BASIC;
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

    @Override
    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public Person getHarvestReportAuthor() {
        return harvestReportAuthor;
    }

    public void setHarvestReportAuthor(final Person harvestReportAuthor) {
        this.harvestReportAuthor = harvestReportAuthor;
    }

    public DateTime getHarvestReportDate() {
        return harvestReportDate;
    }

    public void setHarvestReportDate(final DateTime harvestReportDate) {
        this.harvestReportDate = harvestReportDate;
    }

    public List<HarvestChangeHistory> getChangeHistory() {
        return changeHistory;
    }

    public String getHarvestReportMemo() {
        return harvestReportMemo;
    }

    public void setHarvestReportMemo(final String harvestReportDescription) {
        this.harvestReportMemo = harvestReportDescription;
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

    public PropertyIdentifier getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public void setPropertyIdentifier(final PropertyIdentifier propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    public void setPropertyIdentifier(final String value) {
        this.propertyIdentifier = value != null ? PropertyIdentifier.create(value) : null;
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

    public Integer getSubSpeciesCode() {
        return subSpeciesCode;
    }

    public void setSubSpeciesCode(final Integer subSpeciesCode) {
        this.subSpeciesCode = subSpeciesCode;
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

    public Boolean getFeedingPlace() {
        return feedingPlace;
    }

    public void setFeedingPlace(final Boolean feedingPlace) {
        this.feedingPlace = feedingPlace;
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

    public HuntingClub getHuntingClubForStatistics() {
        return huntingClubForStatistics;
    }

    public void setHuntingClubForStatistics(HuntingClub huntingClubForStatistics) {
        this.huntingClubForStatistics = huntingClubForStatistics;
    }

    public DeerHuntingType getDeerHuntingType() {
        return deerHuntingType;
    }

    public void setDeerHuntingType(final DeerHuntingType deerHuntingType) {
        this.deerHuntingType = deerHuntingType;
    }

    public String getDeerHuntingOtherTypeDescription() {
        return deerHuntingOtherTypeDescription;
    }

    public void setDeerHuntingOtherTypeDescription(final String deerHuntingOtherTypeDescription) {
        this.deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription;
    }
}
