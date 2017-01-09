package fi.riista.feature.harvestpermit.report;

import com.google.common.collect.Lists;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateHistory;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions;
import fi.riista.feature.organization.person.Person;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.joda.time.LocalDate;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Entity
@Access(AccessType.FIELD)
public class HarvestReport extends LifecycleEntity<Long> {

    /**
     * Harvest report is required if harvest report fields exist for that
     * species, and harvest date is REQUIRED_SINCE and after REQUIRED_SINCE.
     * Before REQUIRED_SINCE harvest reports are never required.
     */
    public static final LocalDate REQUIRED_SINCE = new LocalDate(2014, 8, 1);

    public enum State {
        PROPOSED,
        SENT_FOR_APPROVAL,
        APPROVED(false, true),
        REJECTED,
        DELETED;

        private final boolean requiresReason;
        private final boolean requiresPropertyIdentifier;

        State() {
            this(true, false);
        }

        State(boolean requiresReason, boolean requiresPropertyIdentifier) {
            this.requiresReason = requiresReason;
            this.requiresPropertyIdentifier = requiresPropertyIdentifier;
        }

        public boolean requiresReason() {
            return requiresReason;
        }

        public boolean requiresPropertyIdentifier() {
            return requiresPropertyIdentifier;
        }
    }

    private Long id;

    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person author;

    @Fetch(FetchMode.SELECT)
    @OneToMany(mappedBy = "harvestReport")
    private Set<Harvest> harvests = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private State state;

    @OneToMany(mappedBy = "harvestReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HarvestReportStateHistory> stateHistory = Lists.newArrayList();

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestPermit harvestPermit;

    public HarvestReport() {
    }

    public HarvestReport.State findStateAt(Date timestamp) {
        HarvestReportStateHistory lowerBound = HarvestReportStateHistory.withCreationTime(timestamp);

        // Find elements which have strictly lower creationTime
        SortedSet<HarvestReportStateHistory> sorted = LifecycleEntity.sortByCreationTime(getStateHistory());
        SortedSet<HarvestReportStateHistory> headSet = sorted.headSet(lowerBound);

        if (headSet.isEmpty()) {
            // State is unknown at given timestamp
            return null;
        }

        // Result is the greatest item lower than given timestamp
        return headSet.last().getState();
    }

    public boolean isInDeletedState() {
        return getState() == HarvestReport.State.DELETED;
    }

    public boolean isEndOfHuntingReport() {
        return harvestPermit != null
                && harvestPermit.getEndOfHuntingReport() != null
                && harvestPermit.getEndOfHuntingReport().getId().equals(this.id);
    }

    public void addHarvest(Harvest harvest) {
        this.harvests.add(harvest);
    }

    public HarvestReportStateHistory changeState(SystemUser user, State to) {
        HarvestReportStateTransitions.assertChangeState(getRole(user), state, to);
        state = to;
        return new HarvestReportStateHistory(this, state);
    }

    public void initState(SystemUser user) {
        setState(HarvestReportStateTransitions.getInitialState(getRole(user)));
    }

    public List<State> getTransitions(SystemUser user) {
        return HarvestReportStateTransitions.getTransitions(getRole(user), this.getState());
    }

    public boolean canEdit(SystemUser user) {
        return HarvestReportStateTransitions.canEdit(getRole(user), this.getState());
    }

    public boolean canDelete(SystemUser user) {
        return HarvestReportStateTransitions.canDelete(getRole(user), this.getState());
    }

    public boolean canModeratorEdit() {
        return HarvestReportStateTransitions.canModeratorEdit(this.getState());
    }

    public boolean canModeratorDelete() {
        return HarvestReportStateTransitions.canModeratorDelete(this.getState());
    }

    private HarvestReportStateTransitions.ReportRole getRole(SystemUser user) {
        return HarvestReportStateTransitions.getRole(user, this);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_report_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    public Set<Harvest> getHarvests() {
        return harvests;
    }

    public void setHarvests(Set<Harvest> harvests) {
        this.harvests = harvests;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<HarvestReportStateHistory> getStateHistory() {
        return stateHistory;
    }

    public void setStateHistory(List<HarvestReportStateHistory> stateHistory) {
        this.stateHistory = stateHistory;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    public void setHarvestPermit(HarvestPermit harvestPermit) {
        this.harvestPermit = harvestPermit;
    }
}
