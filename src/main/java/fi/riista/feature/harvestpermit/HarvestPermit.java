package fi.riista.feature.harvestpermit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermit extends LifecycleEntity<Long> {

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

    private Long id;

    @FinnishHuntingPermitNumber
    @NotNull
    @Column(nullable = false)
    private String permitNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person originalContactPerson;

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
    private List<HarvestPermitSpeciesAmount> speciesAmounts = Lists.newLinkedList();

    @OneToMany(mappedBy = "harvestPermit", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<HarvestPermitContactPerson> contactPersons = new HashSet<>();

    @OneToMany(mappedBy = "harvestPermit")
    private Set<HarvestReport> harvestReports = new HashSet<>();

    @OneToMany(mappedBy = "harvestPermit")
    private Set<Harvest> harvests = new HashSet<>();

    @Size(max = 255)
    @Column
    private String parsingInfo;

    @Column
    private DateTime lhSyncTime;

    @Column(nullable = false, updatable = false)
    private boolean harvestsAsList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private HarvestReport endOfHuntingReport;

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

    // Helpers -->

    public Optional<HarvestPermitSpeciesAmount> findSpeciesAmount(final int gameSpeciesCode, final LocalDate date) {
        return getSpeciesAmounts().stream()
                .filter(spa -> Objects.requireNonNull(spa).matches(gameSpeciesCode, date))
                .findFirst();
    }

    public boolean isUnavailable() {
        return endOfHuntingReport != null;
    }

    public boolean isPermittedMethodAllowed() {
        return PERMITTED_METHOD_ALLOWED.contains(permitTypeCode);
    }

    public Set<HarvestReport> getUndeletedHarvestReports() {
        return harvestReports == null ? Collections.emptySet() : harvestReports.stream()
                .filter(report -> report.getState() != HarvestReport.State.DELETED)
                .collect(Collectors.toSet());
    }

    public boolean isEndOfHuntingReportRequired() {
        return endOfHuntingReport == null && getSpeciesAmounts().stream().anyMatch(
                spa -> Objects.requireNonNull(spa).isEndOfHuntingReportRequired(getUndeletedHarvestReports()));
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
        if (originalContactPerson.equals(person)) {
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

    public void setSpeciesAmounts(List<HarvestPermitSpeciesAmount> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public Set<HarvestPermitContactPerson> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(Set<HarvestPermitContactPerson> contactPersons) {
        this.contactPersons = contactPersons;
    }

    public void setHarvestReports(Set<HarvestReport> harvestReports) {
        this.harvestReports = harvestReports;
    }

    public Set<Harvest> getHarvests() {
        return harvests;
    }

    public void setHarvests(Set<Harvest> harvests) {
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

    public HarvestReport getEndOfHuntingReport() {
        return endOfHuntingReport;
    }

    public void setEndOfHuntingReport(HarvestReport endOfHuntingReport) {
        this.endOfHuntingReport = endOfHuntingReport;
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
