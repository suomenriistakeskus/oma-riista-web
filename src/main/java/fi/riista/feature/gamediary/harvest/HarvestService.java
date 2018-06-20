package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.CommitHookService;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationFactory;
import fi.riista.feature.gamediary.harvest.mutation.HarvestUpdater;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.harvestpermit.report.email.HarvestReportNotificationService;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.organization.person.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class HarvestService {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestService.class);

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestMutationFactory harvestMutationFactory;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private HarvestReportNotificationService harvestReportNotificationService;

    @Resource
    private CommitHookService commitHookService;

    private static void assertNotAcceptedToPermit(final Harvest harvest, final SystemUser activeUser) {
        if (harvest.getHarvestPermit() != null && harvest.isAcceptedToHarvestPermit()) {
            final Person person = activeUser.getPerson();

            if (person == null || !harvest.getHarvestPermit().hasContactPerson(person)) {
                throw new RuntimeException("Cannot delete harvest which is accepted to permit.");
            }
        }
    }

    private static void assertNotAttachedToHuntingDay(final Harvest harvest) {
        if (harvest.getHuntingDayOfGroup() != null) {
            throw new RuntimeException("Cannot delete harvest with an associated hunting day.");
        }
    }

    // harvestSpecVersion should be defined for mobile and undefined for web
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean canBusinessFieldsBeUpdated(final Person activePerson,
                                              @Nonnull final Harvest harvest,
                                              final HarvestSpecVersion harvestSpecVersion) {
        Objects.requireNonNull(harvest, "harvest is null");

        // for mobile should be canEdit=false if attached to hunting day
        final Predicate<Harvest> groupHuntingStatusLockedTester = createGroupHuntingStatusLockedTester();
        final Predicate<Harvest> contactPersonTester = HarvestLockedCondition.createContactPersonTester(activePerson);

        return HarvestLockedCondition.canEdit(activePerson, harvest, harvestSpecVersion,
                groupHuntingStatusLockedTester, contactPersonTester);
    }

    private Predicate<Harvest> createGroupHuntingStatusLockedTester() {
        return harvest -> clubHuntingStatusService.isDiaryEntryLocked(harvest);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HarvestChangeHistory updateMutableFields(final Harvest harvest,
                                                    final MobileHarvestDTO dto,
                                                    final SystemUser activeUser,
                                                    final boolean businessFieldsCanBeUpdated) {
        final HarvestChangeHistory historyEvent;

        if (businessFieldsCanBeUpdated) {
            final HarvestUpdater harvestMutator = HarvestUpdater.create(harvestMutationFactory, harvest, activeUser);
            harvestMutator.execute(harvest, dto);
            historyEvent = harvestMutator.createHistoryEventIfRequiredForPerson(harvest);

            if (harvestMutator.shouldSendHarvestNotificationEmail(harvest)) {
                commitHookService.runInTransactionAfterCommit(() -> {
                    harvestReportNotificationService.sendNotificationForHarvest(harvest.getId());
                });
            }
        } else {
            historyEvent = null;

            if (dto.isCanEdit()) {
                LOG.warn("Could not update read-only harvest id: " + dto.getId());
            }
        }

        harvest.setDescription(dto.getDescription());

        return historyEvent;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HarvestChangeHistory updateMutableFields(final Harvest harvest,
                                                    final HarvestDTO dto,
                                                    final SystemUser activeUser,
                                                    final boolean businessFieldsCanBeUpdated) {
        final Person currentPerson = activeUser.getPerson();
        final HarvestChangeHistory historyEvent;

        if (businessFieldsCanBeUpdated) {
            final HarvestUpdater harvestMutator = HarvestUpdater.create(harvestMutationFactory, harvest, activeUser);
            final HarvestReportingType reportingType = harvestMutator.execute(harvest, dto);
            historyEvent = harvestMutator.createHistoryEventIfRequired(activeUser, harvest, reportingType, dto.getModeratorReasonForChange());

            if (harvestMutator.shouldSendHarvestNotificationEmail(harvest)) {
                commitHookService.runInTransactionAfterCommit(() -> {
                    harvestReportNotificationService.sendNotificationForHarvest(harvest.getId());
                });
            }

        } else {
            historyEvent = null;

            if (dto.isCanEdit()) {
                LOG.warn("Could not update read-only harvest id: " + dto.getId());
            }
        }

        if (harvest.isAuthorOrActor(currentPerson)) {
            harvest.setDescription(dto.getDescription());
        }

        if (activeUser.isModeratorOrAdmin()) {
            harvest.setLukeStatus(dto.getLukeStatus());

            if (StringUtils.hasText(dto.getHarvestReportMemo())) {
                harvest.setHarvestReportMemo(dto.getHarvestReportMemo());
            }
        }

        return historyEvent;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteHarvest(final Harvest harvest, final SystemUser activeUser) {
        if (harvest.isHarvestReportApproved() || harvest.isHarvestReportRejected()) {
            throw new RuntimeException("Cannot delete harvest with an associated harvest report.");
        }

        assertNotAcceptedToPermit(harvest, activeUser);
        assertNotAttachedToHuntingDay(harvest);

        harvestRepository.delete(harvest);
    }
}
