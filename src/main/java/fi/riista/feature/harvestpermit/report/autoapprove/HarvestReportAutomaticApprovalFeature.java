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
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class HarvestReportAutomaticApprovalFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportAutomaticApprovalFeature.class);

    private static final LocalDate HUNTING_YEAR_2017 = new LocalDate(2017, 8, 1);
    private static final LocalDate HUNTING_YEAR_2018 = new LocalDate(2018, 8, 1);
    private static final LocalDate HUNTING_YEAR_2020 = new LocalDate(2020, 8, 1);
    private static final LocalDate HUNTING_YEAR_2021 = new LocalDate(2021, 8, 1);
    private static final LocalDate AUTO_APPROVE_COMMON_EIDER = new LocalDate(2020, 6, 1);

    private static final Map<Integer, LocalDate> AUTO_APPROVE_ENABLED =
            ImmutableMap.<Integer, LocalDate>builder()
                    .put(GameSpecies.OFFICIAL_CODE_WILD_BOAR, HUNTING_YEAR_2017)
                    .put(GameSpecies.OFFICIAL_CODE_ROE_DEER, HUNTING_YEAR_2017)
                    .put(GameSpecies.OFFICIAL_CODE_GREY_SEAL, HUNTING_YEAR_2018)
                    .put(GameSpecies.OFFICIAL_CODE_BEAN_GOOSE, HUNTING_YEAR_2018)
                    .put(GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT, HUNTING_YEAR_2018)
                    .put(GameSpecies.OFFICIAL_CODE_WIGEON, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_PINTAIL, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_GARGANEY, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_SHOVELER, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_POCHARD, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_TUFTED_DUCK, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_COMMON_EIDER, AUTO_APPROVE_COMMON_EIDER)
                    .put(GameSpecies.OFFICIAL_CODE_LONG_TAILED_DUCK, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_RED_BREASTED_MERGANSER, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_GOOSANDER, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_COOT, HUNTING_YEAR_2020)
                    .put(GameSpecies.OFFICIAL_CODE_RINGED_SEAL, HUNTING_YEAR_2021)
                    .put(GameSpecies.OFFICIAL_CODE_GREYLAG_GOOSE, HUNTING_YEAR_2021)
                    .build();

    public static final String AUTO_ACCEPT_REASON = "Automaattihyväksytty";

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional
    public void runAutoApprove() {
        final DateTime harvestReportModifiedEarlierThan = DateUtil.now().minusDays(1).toDateTime();
        AUTO_APPROVE_ENABLED.forEach((speciesCode, enabledFrom) ->
                findAndChangeState(harvestReportModifiedEarlierThan, speciesCode, enabledFrom));
    }

    private void findAndChangeState(final DateTime harvestReportModifiedEarlierThan,
                                    final int speciesCode,
                                    final LocalDate enabledFrom) {

        final QHarvest HARVEST = QHarvest.harvest;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final List<Harvest> reports = queryFactory.selectFrom(HARVEST)
                .join(HARVEST.species, SPECIES)
                .where(HARVEST.pointOfTime.goe(DateUtil.toDateTimeNullSafe(enabledFrom)),
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
