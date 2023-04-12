package fi.riista.feature.harvestpermit.season;

import com.google.common.collect.Streams;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.partitioningBy;

@Component
public class HarvestQuotaService {

    @Resource
    private HarvestAreaRepository areaRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestQuotaDTO> createOrUpdateQuotas(final List<HarvestQuotaDTO> dtos, final HarvestSeason season) {
        final Map<Boolean, List<HarvestQuotaDTO>> quotaMapping = dtos.stream()
                .collect(partitioningBy(s -> s.getId() == null));

        final List<HarvestQuota> created = createQuotas(quotaMapping.get(true), season);
        final List<HarvestQuota> updated = updateQuotas(quotaMapping.get(false));

        final List<HarvestQuota> all = Streams.concat(created.stream(), updated.stream()).collect(Collectors.toList());
        final List<HarvestQuota> saved = harvestQuotaRepository.saveAll(all);
        return saved.stream().map(HarvestQuotaDTO::create).collect(Collectors.toList());
    }

    private List<HarvestQuota> createQuotas(final List<HarvestQuotaDTO> createList, final HarvestSeason season) {
        return createList.stream()
                .map(quotaDTO -> {
                    final HarvestQuota quota = new HarvestQuota();
                    activeUserService.assertHasPermission(quota, EntityPermission.CREATE);

                    quota.setHarvestSeason(season);
                    quota.setQuota(quotaDTO.getQuota());

                    final HarvestAreaDTO areaDTO = Objects.requireNonNull(quotaDTO.getHarvestArea());
                    final HarvestArea area = areaRepository.findByTypeAndOfficialCode(areaDTO.getHarvestAreaType(), areaDTO.getOfficialCode());
                    checkArgument(area != null, "Harvest area must exist for quotas");
                    quota.setHarvestArea(area);

                    return quota;
                }).collect(Collectors.toList());
    }

    private List<HarvestQuota> updateQuotas(final List<HarvestQuotaDTO> updateList) {
        return updateList.stream()
                .map(quotaDTO -> {
                    final HarvestQuota quota = requireEntityService.requireHarvestQuota(quotaDTO.getId(), EntityPermission.UPDATE);
                    DtoUtil.assertNoVersionConflict(quota, quotaDTO);

                    quota.setQuota(quotaDTO.getQuota());

                    return quota;
                }).collect(Collectors.toList());

    }
}
