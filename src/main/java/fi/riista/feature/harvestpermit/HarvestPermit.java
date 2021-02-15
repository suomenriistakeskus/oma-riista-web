package fi.riista.feature.harvestpermit;

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
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.F;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermit extends LifecycleEntity<Long> implements HasHarvestReportState {

    public static final String ID_COLUMN_NAME = "harvest_permit_id";

    private Long id;

    @FinnishHuntingPermitNumber
    @NotNull
    @Column(nullable = false)
    private String permitNumber;

    @Column(nullable = false)
    private int permitYear;

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
    private final List<HarvestPermitSpeciesAmount> speciesAmounts = new LinkedList<>();

    @OneToMany(mappedBy = "harvestPermit", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<HarvestPermitContactPerson> contactPersons = new LinkedList<>();

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

    @Embedded
    @Valid
    private PermitHolder permitHolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permit_holder_id")
    private HuntingClub huntingClub;

    @ManyToMany
    @JoinTable(name = "harvest_permit_partners",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Organisation.ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)})
    private Set<HuntingClub> permitPartners = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestPermit originalPermit;

    @OneToMany(mappedBy = "harvestPermit")
    private final Set<HuntingClubGroup> permitGroups = new HashSet<>();

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

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    @Column(columnDefinition = "TEXT")
    private String endOfHuntingReportComments;

    @AssertTrue
    public boolean isHarvestReportFieldsConsistent() {
        return F.allNull(this.harvestReportAuthor, this.harvestReportState, this.harvestReportDate, this.harvestReportModeratorOverride) ||
                F.allNotNull(this.harvestReportAuthor, this.harvestReportState, this.harvestReportDate, this.harvestReportModeratorOverride);
    }

    @AssertTrue
    public boolean isPermitHolderSetForMooselike() {
        return !(isMooselikePermitType() || isAmendmentPermit()) || this.permitHolder != null;
    }

    // Helpers -->

    public boolean hasSpeciesAmount(final int gameSpeciesCode, final LocalDate date) {
        return getSpeciesAmounts().stream().anyMatch(spa -> spa.matches(gameSpeciesCode, date));
    }

    public boolean isPermittedMethodAllowed() {
        return PermitTypeCode.isPermittedMethodAllowed(permitTypeCode);
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

    public boolean canAddHarvest(final @Nonnull SystemUser activeUser) {
        return !isHarvestReportDone() &&
                (activeUser.isModeratorOrAdmin() || hasContactPerson(activeUser.getPerson()));
    }

    public boolean canCreateEndOfHuntingReport(final @Nonnull SystemUser activeUser) {
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
        return PermitTypeCode.isMooselikePermitTypeCode(this.getPermitTypeCode());
    }

    public boolean isApplicableForMooseDataCardImport() {
        // Only "100" permit type code is permitted.
        return isMooselikePermitType();
    }

    public boolean isPermitHolderOrPartner(final HuntingClub club) {
        return isPermitHolder(club) || isPermitPartner(club);
    }

    public boolean isPermitHolder(final HuntingClub club) {
        return huntingClub != null && Objects.equals(huntingClub.getId(), club.getId());
    }

    public boolean isPermitPartner(final HuntingClub club) {
        return permitPartners.stream().anyMatch(p -> Objects.equals(p.getId(), club.getId()));
    }

    public boolean isAmendmentPermit() {
        return PermitTypeCode.isAmendmentPermitTypeCode(this.permitTypeCode);
    }

    public boolean isNestRemovalPermit() {
        return PermitTypeCode.isNestRemovalPermitTypeCode(this.permitTypeCode);
    }

    public boolean hasContactPerson(final Person person) {
        if (originalContactPerson != null && originalContactPerson.equals(person)) {
            return true;
        }
        for (final HarvestPermitContactPerson cp : contactPersons) {
            if (cp.getContactPerson().equals(person)) {
                return true;
            }
        }
        return false;
    }

    public void addHarvest(final Harvest h) {
        this.harvests.add(h);
    }

    // Querydsl delegates -->

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression matchesPermitYear(final QHarvestPermit permit, final int year) {
        return permit.permitYear.eq(year);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression hasRhy(final QHarvestPermit permit, final long rhyId) {
        return permit.rhy.id.eq(rhyId);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression hasRelatedRhy(final QHarvestPermit permit, final long rhyId) {
        return permit.relatedRhys.any().id.eq(rhyId);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression hasRhyOrRelatedRhy(final QHarvestPermit permit, final long rhyId) {
        return permit.hasRhy(rhyId).or(permit.hasRelatedRhy(rhyId));
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression isMooselikePermit(final QHarvestPermit permit) {
        return permit.permitTypeCode.eq(PermitTypeCode.MOOSELIKE);
    }

    @QueryDelegate(HarvestPermit.class)
    public static BooleanExpression isMooselikeOrAmendmentPermit(final QHarvestPermit permit) {
        return permit.permitTypeCode.in(PermitTypeCode.MOOSELIKE, PermitTypeCode.MOOSELIKE_AMENDMENT);
    }

    // CONSTRUCTORS

    public static HarvestPermit create(@Nonnull final String permitNumber) {
        requireNonNull(permitNumber);
        return new HarvestPermit(permitNumber, DocumentNumberUtil.extractYear(permitNumber));
    }

    // For Hibernate
    public HarvestPermit() {
    }

    private HarvestPermit(final String permitNumber, final int permitYear) {
        this.permitNumber = permitNumber;
        this.permitYear = permitYear;
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
    public void setId(final Long id) {
        this.id = id;
    }

    public String getPermitNumber() {
        return permitNumber;
    }


    public int getPermitYear() {
        return permitYear;
    }

    public Person getOriginalContactPerson() {
        return originalContactPerson;
    }

    public void setOriginalContactPerson(final Person originalContactPerson) {
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

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(final String permitType) {
        this.permitType = permitType;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
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

    public void setParsingInfo(final String parsingInfo) {
        this.parsingInfo = parsingInfo;
    }

    public DateTime getLhSyncTime() {
        return lhSyncTime;
    }

    public void setLhSyncTime(final DateTime lhSyncTime) {
        this.lhSyncTime = lhSyncTime;
    }

    public boolean isHarvestsAsList() {
        return harvestsAsList;
    }

    public void setHarvestsAsList(final boolean harvestsAsList) {
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

    public PermitHolder getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolder permitHolder) {
        this.permitHolder = permitHolder;
    }

    public HuntingClub getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(final HuntingClub huntingClub) {
        this.huntingClub = huntingClub;
    }

    public Set<HuntingClub> getPermitPartners() {
        return permitPartners;
    }

    public void setPermitPartners(final Set<HuntingClub> permitPartners) {
        this.permitPartners = permitPartners;
    }

    public HarvestPermit getOriginalPermit() {
        return originalPermit;
    }

    public void setOriginalPermit(final HarvestPermit originalPermit) {
        this.originalPermit = originalPermit;
    }

    public String getPrintingUrl() {
        return printingUrl;
    }

    public void setPrintingUrl(final String printingUrl) {
        this.printingUrl = printingUrl;
    }

    public GISHirvitalousalue getMooseArea() {
        return mooseArea;
    }

    public void setMooseArea(final GISHirvitalousalue mooseArea) {
        this.mooseArea = mooseArea;
    }

    public Set<Riistanhoitoyhdistys> getRelatedRhys() {
        return relatedRhys;
    }

    public void setRelatedRhys(final Set<Riistanhoitoyhdistys> relatedRhys) {
        this.relatedRhys = relatedRhys;
    }

    public Integer getPermitAreaSize() {
        return permitAreaSize;
    }

    public void setPermitAreaSize(final Integer permitAreaSize) {
        this.permitAreaSize = permitAreaSize;
    }

    // Following collection getters exposed in package-private scope only for property introspection.

    Set<HuntingClubGroup> getPermitGroups() {
        return permitGroups;
    }

    public String getEndOfHuntingReportComments() {
        return endOfHuntingReportComments;
    }

    public void setEndOfHuntingReportComments(final String endOfHuntingReportComments) {
        this.endOfHuntingReportComments = endOfHuntingReportComments;
    }

}
