package fi.riista.feature.harvestpermit;

import com.google.common.collect.Sets;
import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.report.HasHarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermit extends LifecycleEntity<Long> implements HasHarvestReportState {

    public static final String ID_COLUMN_NAME = "harvest_permit_id";

    /**
     * These permit types possibly have permitted methods which are otherwise
     * illegal.
     */
    private static final Set<String> PERMITTED_METHOD_ALLOWED = Sets.newHashSet("300", "310", "345", "370");

    private static final Set<String> PERMIT_TYPES_AS_LIST = Sets.newHashSet(
            "200", "210", "250", "251", "253", "300", "310", "345", "370");

    public static final String MOOSELIKE_PERMIT_TYPE = "100";
    public static final String MOOSELIKE_AMENDMENT_PERMIT_TYPE = "190";
    public static final LocalisedString MOOSELIKE_PERMIT_NAME = LocalisedString.of("Hirvieläinten pyyntilupa", "Jaktlicens för hjortdjur");

    private static final Set<String> RESOLVE_PERMIT_HOLDER_AND_PARTNER = Collections.singleton(MOOSELIKE_PERMIT_TYPE);

    public static boolean checkIsHarvestsAsList(String permitTypeCode) {
        return PERMIT_TYPES_AS_LIST.contains(permitTypeCode);
    }

    public static boolean checkShouldResolvePermitHolder(String permitTypeCode) {
        return RESOLVE_PERMIT_HOLDER_AND_PARTNER.contains(permitTypeCode);
    }

    public static boolean checkShouldResolvePermitPartners(String permitTypeCode) {
        return RESOLVE_PERMIT_HOLDER_AND_PARTNER.contains(permitTypeCode);
    }

    public static boolean isMooselikePermitTypeCode(String permitTypeCode) {
        return MOOSELIKE_PERMIT_TYPE.equals(permitTypeCode);
    }

    public static boolean isAmendmentPermitTypeCode(String permitTypeCode) {
        return MOOSELIKE_AMENDMENT_PERMIT_TYPE.equals(permitTypeCode);
    }

    public static LocalDate getDefaultMooselikeBeginDate(final int huntingYear) {
        return new LocalDate(huntingYear, 9, 1);
    }

    public static LocalDate getDefaultMooselikeEndDate(final int huntingYear) {
        return new LocalDate(huntingYear + 1, 1, 15);
    }

    private Long id;

    @FinnishHuntingPermitNumber
    @NotNull
    @Column(nullable = false)
    private String permitNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person originalContactPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    private PermitDecision permitDecision;

    /**
     * RHY johon lupa on myönnetty
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    @Size(max = 255)
    @NotNull
    @Column(nullable = false)
    private String permitType;

    @Size(min = 3, max = 3)
    @NotNull
    @Column(nullable = false, length = 3)
    private String permitTypeCode;

    @OneToMany(mappedBy = "harvestPermit", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<HarvestPermitSpeciesAmount> speciesAmounts = new LinkedList<>();

    @OneToMany(mappedBy = "harvestPermit", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<HarvestPermitContactPerson> contactPersons = new LinkedList<>();

    @OneToMany(mappedBy = "harvestPermit")
    private List<Harvest> harvests = new LinkedList<>();

    @Size(max = 255)
    @Column
    private String parsingInfo;

    @Column
    private DateTime lhSyncTime;

    @Column(nullable = false, updatable = false)
    private boolean harvestsAsList;

    @Column
    @Enumerated(EnumType.STRING)
    private HarvestReportState harvestReportState;

    @Column
    private DateTime harvestReportDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person harvestReportAuthor;

    @Column
    private Boolean harvestReportModeratorOverride;

    @OneToMany(mappedBy = "harvestPermit")
    private Set<MooseHuntingSummary> mooseHuntingSummaries;

    @ManyToOne(fetch = FetchType.LAZY)
    private HuntingClub permitHolder;

    @ManyToMany
    @JoinTable(name = "harvest_permit_partners",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Organisation.ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)}
    )
    private Set<HuntingClub> permitPartners = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestPermit originalPermit;

    @OneToMany(mappedBy = "harvestPermit")
    private Set<HuntingClubGroup> permitGroups = new HashSet<>();

    /**
     * Lupapäätöksen tulostuksen URL
     */
    @URL
    @Size(max = 2048) // max url length in IE
    @Column(length = 2048)
    private String printingUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private GISHirvitalousalue mooseArea;

    /**
     * RHY:t joille lupa annettu tiedoksi
     */
    @ManyToMany
    @JoinTable(name = "harvest_permit_rhys",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Organisation.ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)}
    )
    private Set<Riistanhoitoyhdistys> relatedRhys = new HashSet<>();

    // Area size in full hectares.
    @Column
    private Integer permitAreaSize;

    @AssertTrue
    public boolean isHarvestReportFieldsConsistent() {
        return F.allNull(this.harvestReportAuthor, this.harvestReportState, this.harvestReportDate, this.harvestReportModeratorOverride) ||
                F.allNotNull(this.harvestReportAuthor, this.harvestReportState, this.harvestReportDate, this.harvestReportModeratorOverride);
    }

    // Helpers -->

    public boolean hasSpeciesAmount(final int gameSpeciesCode, final LocalDate date) {
        return getSpeciesAmounts().stream().anyMatch(spa -> spa.matches(gameSpeciesCode, date));
    }

    public boolean isPermittedMethodAllowed() {
        return PERMITTED_METHOD_ALLOWED.contains(permitTypeCode);
    }

    @Nonnull
    public Harvest.StateAcceptedToHarvestPermit getStateAcceptedToPermit(@Nullable final Person currentPerson) {
        if (isHarvestReportApproved() || isHarvestReportRejected()) {
            // no-one can add harvest for permit when report has been approved or rejected
            return Harvest.StateAcceptedToHarvestPermit.REJECTED;
        }

        if (isHarvestReportSentForApproval()) {
            // only moderator can add harvest to permit with report sent for approval
            return currentPerson == null
                    ? Harvest.StateAcceptedToHarvestPermit.ACCEPTED
                    : Harvest.StateAcceptedToHarvestPermit.REJECTED;
        }

        // accept harvest for permit automatically as permit contact person or moderator
        return currentPerson == null || hasContactPerson(currentPerson)
                ? Harvest.StateAcceptedToHarvestPermit.ACCEPTED
                : Harvest.StateAcceptedToHarvestPermit.PROPOSED;
    }

    public List<Harvest> getAcceptedHarvestForEndOfHuntingReport() {
        return this.harvests.stream()
                .filter(h -> !h.isHarvestReportRejected() && h.isAcceptedToHarvestPermit())
                .collect(Collectors.toList());
    }

    public boolean hasHarvestProposedToPermit() {
        return this.harvests.stream().anyMatch(h -> h.getStateAcceptedToHarvestPermit() == Harvest.StateAcceptedToHarvestPermit.PROPOSED);
    }

    public boolean isHarvestReportAllowed() {
        return !isMooselikePermitType() && !isAmendmentPermit();
    }

    public boolean canAddHarvest(final SystemUser activeUser) {
        return !isHarvestReportDone() &&
                (activeUser.isModeratorOrAdmin() || hasContactPerson(activeUser.getPerson()));
    }

    public boolean canCreateEndOfHuntingReport(final SystemUser activeUser) {
        return !isHarvestReportDone() && !hasHarvestProposedToPermit() &&
                (activeUser.isModeratorOrAdmin() || hasContactPerson(activeUser.getPerson()));
    }

    public boolean canRemoveEndOfHuntingReport(final SystemUser activeUser) {
        return isHarvestReportDone() &&
                (activeUser.isModeratorOrAdmin() || (!isHarvestReportApproved() && hasContactPerson(activeUser.getPerson())));
    }

    public boolean canAcceptOrRejectEndOfHuntingReport(final SystemUser activeUser) {
        return isHarvestReportDone() && activeUser.isModeratorOrAdmin();
    }

    public boolean isMooselikePermitType() {
        return isMooselikePermitTypeCode(this.getPermitTypeCode());
    }

    public boolean isApplicableForMooseDataCardImport() {
        // Only "100" permit type code is permitted.
        return isMooselikePermitType();
    }

    public boolean isPermitHolderOrPartner(HuntingClub club) {
        return isPermitHolder(club) || isPermitPartner(club);
    }

    public boolean isPermitHolder(HuntingClub club) {
        return permitHolder != null && Objects.equals(permitHolder.getId(), club.getId());
    }

    public boolean isPermitPartner(HuntingClub club) {
        return permitPartners.stream().anyMatch(p -> Objects.equals(p.getId(), club.getId()));
    }

    public boolean isAmendmentPermit() {
        return isAmendmentPermitTypeCode(this.permitTypeCode);
    }

    public boolean hasContactPerson(Person person) {
        if (originalContactPerson != null && originalContactPerson.equals(person)) {
            return true;
        }
        for (HarvestPermitContactPerson cp : contactPersons) {
            if (cp.getContactPerson().equals(person)) {
                return true;
            }
        }
        return false;
    }

    public void addHarvest(Harvest h) {
        this.harvests.add(h);
    }

    // Querydsl delegates -->

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression hasRhy(QHarvestPermit permit, long rhyId) {
        return permit.rhy.id.eq(rhyId);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression hasRelatedRhy(QHarvestPermit permit, long rhyId) {
        return permit.relatedRhys.any().id.eq(rhyId);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression hasRhyOrRelatedRhy(QHarvestPermit permit, long rhyId) {
        return permit.hasRhy(rhyId).or(permit.hasRelatedRhy(rhyId));
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression isMooselikePermit(QHarvestPermit permit) {
        return permit.permitTypeCode.eq(HarvestPermit.MOOSELIKE_PERMIT_TYPE);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression isMooselikeOrAmendmentPermit(QHarvestPermit permit) {
        return permit.permitTypeCode.in(MOOSELIKE_PERMIT_TYPE, MOOSELIKE_AMENDMENT_PERMIT_TYPE);
    }

    // Accessors -->

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

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public Person getOriginalContactPerson() {
        return originalContactPerson;
    }

    public void setOriginalContactPerson(Person originalContactPerson) {
        this.originalContactPerson = originalContactPerson;
    }

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(final PermitDecision permitDecision) {
        this.permitDecision = permitDecision;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public List<HarvestPermitSpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<HarvestPermitContactPerson> getContactPersons() {
        return contactPersons;
    }

    public List<Harvest> getHarvests() {
        return harvests;
    }

    void setHarvests(final List<Harvest> harvests) {
        this.harvests = harvests;
    }

    public String getParsingInfo() {
        return parsingInfo;
    }

    public void setParsingInfo(String parsingInfo) {
        this.parsingInfo = parsingInfo;
    }

    public DateTime getLhSyncTime() {
        return lhSyncTime;
    }

    public void setLhSyncTime(DateTime lhSyncTime) {
        this.lhSyncTime = lhSyncTime;
    }

    public boolean isHarvestsAsList() {
        return harvestsAsList;
    }

    public void setHarvestsAsList(boolean harvestsAsList) {
        this.harvestsAsList = harvestsAsList;
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

    public Boolean getHarvestReportModeratorOverride() {
        return harvestReportModeratorOverride;
    }

    public void setHarvestReportModeratorOverride(final Boolean harvestReportModeratorOverride) {
        this.harvestReportModeratorOverride = harvestReportModeratorOverride;
    }

    public HuntingClub getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(HuntingClub permitHolder) {
        this.permitHolder = permitHolder;
    }

    public Set<HuntingClub> getPermitPartners() {
        return permitPartners;
    }

    public void setPermitPartners(Set<HuntingClub> permitPartners) {
        this.permitPartners = permitPartners;
    }

    public HarvestPermit getOriginalPermit() {
        return originalPermit;
    }

    public void setOriginalPermit(HarvestPermit originalPermit) {
        this.originalPermit = originalPermit;
    }

    public String getPrintingUrl() {
        return printingUrl;
    }

    public void setPrintingUrl(String printingUrl) {
        this.printingUrl = printingUrl;
    }

    public GISHirvitalousalue getMooseArea() {
        return mooseArea;
    }

    public void setMooseArea(GISHirvitalousalue mooseArea) {
        this.mooseArea = mooseArea;
    }

    public Set<Riistanhoitoyhdistys> getRelatedRhys() {
        return relatedRhys;
    }

    public void setRelatedRhys(Set<Riistanhoitoyhdistys> relatedRhys) {
        this.relatedRhys = relatedRhys;
    }

    public Integer getPermitAreaSize() {
        return permitAreaSize;
    }

    public void setPermitAreaSize(Integer permitAreaSize) {
        this.permitAreaSize = permitAreaSize;
    }

    // Following collection getters exposed in package-private scope only for property introspection.

    Set<HuntingClubGroup> getPermitGroups() {
        return permitGroups;
    }

}
