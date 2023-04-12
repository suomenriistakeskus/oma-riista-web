package fi.riista.feature.harvestpermit.report;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryDTO;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestQuotaRepository;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestReportModeratorFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestReportModeratorService harvestReportModeratorService;

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public Map<String, Object> admin() {
        final List<HarvestSeason> seasons = harvestSeasonRepository.findAll();
        if (seasons.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<HarvestSeason, List<HarvestQuota>> seasonToQuotas =
                harvestQuotaRepository.findByHarvestSeasonIn(seasons).stream()
                        .collect(groupingBy(HarvestQuota::getHarvestSeason, toList()));

        final List<HarvestSeasonDTO> seasonDTOs =
                F.mapNonNullsToList(seasons, season -> HarvestSeasonDTO.createWithSpeciesAndQuotas(season, seasonToQuotas.get(season)));

        return new ImmutableMap.Builder<String, Object>()
                .put("seasons", seasonDTOs)
                .build();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void changeHarvestReportState(final HarvestReportStateChangeDTO dto) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (!activeUser.isModeratorOrAdmin()) {
            throw new IllegalStateException();
        }

        final Harvest harvest = requireEntityService.requireHarvest(dto.getHarvestId(), EntityPermission.READ);
        harvestReportModeratorService.changeHarvestReportState(dto, activeUser, harvest);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<HarvestChangeHistoryDTO> getChangeHistory(final long harvestId) {
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.READ);

        final List<HarvestChangeHistory> eventList = harvestChangeHistoryRepository.findByHarvest(harvest);
        final Map<Long, SystemUser> userMapping = createUserMapping(eventList);

        return eventList.stream().map(event -> {
            final SystemUser user = event.getUserId() != null ? userMapping.get(event.getUserId()) : null;

            return new HarvestChangeHistoryDTO(
                    event.getPointOfTime(),
                    event.getReasonForChange(),
                    event.getHarvestReportState(),
                    user);
        }).collect(toList());
    }

    private Map<Long, SystemUser> createUserMapping(List<HarvestChangeHistory> changeHistory) {
        final HashSet<Long> userIds = F.mapNonNullsToSet(changeHistory, HarvestChangeHistory::getUserId);

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllAsList(QSystemUser.systemUser.id.in(userIds)).stream()
                .collect(Collectors.toMap(SystemUser::getId, identity()));
    }
}
