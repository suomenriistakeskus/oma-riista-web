package fi.riista.feature.harvestpermit.report.autoapprove;

import com.google.common.collect.ImmutableMap;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class HarvestReportAutomaticApprovalFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportAutomaticApprovalFeature.class);

    private static final LocalDate HUNTING_YEAR_2017 = new LocalDate(2017, 8, 1);
    private static final LocalDate HUNTING_YEAR_2018 = new LocalDate(2018, 8, 1);

    private static final Map<Integer, LocalDate> AUTO_APPROVE_ENABLED = ImmutableMap.of(
            GameSpecies.OFFICIAL_CODE_WILD_BOAR, HUNTING_YEAR_2017,
            GameSpecies.OFFICIAL_CODE_ROE_DEER, HUNTING_YEAR_2017,
            GameSpecies.OFFICIAL_CODE_GREY_SEAL, HUNTING_YEAR_2018,
            GameSpecies.OFFICIAL_CODE_BEAN_GOOSE, HUNTING_YEAR_2018,
            GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT, HUNTING_YEAR_2018);

    public static final String AUTO_ACCEPT_REASON = "Automaattihyväksytty";

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional
    public void runAutoApprove() {
        final Date harvestReportModifiedEarlierThan = DateUtil.now().minusDays(1).toDate();
        AUTO_APPROVE_ENABLED.forEach((speciesCode, enabledFrom) ->
                findAndChangeState(harvestReportModifiedEarlierThan, speciesCode, DateUtil.toDateNullSafe(enabledFrom)));
    }

    private void findAndChangeState(final Date harvestReportModifiedEarlierThan,
                                    final int speciesCode,
                                    final Date enabledFrom) {

        final QHarvest HARVEST = QHarvest.harvest;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final List<Harvest> reports = queryFactory.selectFrom(HARVEST)
                .join(HARVEST.species, SPECIES)
                .where(HARVEST.pointOfTime.goe(enabledFrom),
                        HARVEST.lifecycleFields.modificationTime.lt(harvestReportModifiedEarlierThan),
                        HARVEST.harvestReportState.eq(HarvestReportState.SENT_FOR_APPROVAL),
                        SPECIES.officialCode.eq(speciesCode))
                .fetch();

        reports.forEach(this::approve);
    }

    private void approve(final Harvest harvest) {
        LOG.info("Auto approve harvest id:{} rev:{}", harvest.getId(), harvest.getConsistencyVersion());
        harvest.setHarvestReportState(HarvestReportState.APPROVED);

        final HarvestChangeHistory historyEvent = new HarvestChangeHistory();
        historyEvent.setHarvest(harvest);
        historyEvent.setHarvestReportState(HarvestReportState.APPROVED);
        historyEvent.setPointOfTime(DateUtil.now());
        historyEvent.setReasonForChange(AUTO_ACCEPT_REASON);
        historyEvent.setUserId(ActiveUserService.SCHEDULED_TASK_USER_ID);

        harvestChangeHistoryRepository.save(historyEvent);
    }
}
