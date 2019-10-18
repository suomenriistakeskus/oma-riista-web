package fi.riista.feature.harvestpermit.statistics;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.hta.HirvitalousalueService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.feature.huntingclub.permit.statistics.PermitAndLocationId;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Component
public class MoosePermitStatisticsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestCountService harvestCountService;

    @Resource
    private MoosePermitStatisticsAmountService permitAmountService;

    @Resource
    private ClubHuntingSummaryBasicInfoService summaryService;

    @Resource
    private MoosePermitStatisticsListService moosePermitStatisticsListService;

    @Resource
    private RiistanhoitoyhdistysNameService riistanhoitoyhdistysNameService;

    @Resource
    private HirvitalousalueService hirvitalousalueService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public List<MoosePermitStatisticsDTO> getRhyStatistics(final long permitId, final int speciesCode, Locale locale) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final MoosePermitStatisticsI18n i18n = createI18n(locale, speciesCode);
        final String rhyOfficialCode = permit.getRhy().getOfficialCode();

        final int huntingYear = harvestPermitSpeciesAmountRepository
                .getOneByHarvestPermitAndSpeciesCode(permit, speciesCode)
                .findUnambiguousHuntingYear()
                .orElseThrow(IllegalStateException::new);

        return calculateByPermit(i18n, speciesCode, huntingYear, MoosePermitStatisticsOrganisationType.RHY, rhyOfficialCode)
                .build(MoosePermitStatisticsGroupBy.RHY_PERMIT, true);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public List<MoosePermitStatisticsDTO> calculate(final @Nonnull Locale locale,
                                                    final @Nonnull MoosePermitStatisticsReportType reportType,
                                                    final @Nonnull MoosePermitStatisticsGroupBy groupBy,
                                                    final boolean includeGrandTotal,
                                                    final int speciesCode, final int huntingYear,
                                                    final @Nonnull MoosePermitStatisticsOrganisationType orgType,
                                                    final @Nonnull String orgCode) {
        requireNonNull(locale);
        requireNonNull(reportType);
        requireNonNull(groupBy);
        requireNonNull(orgType);
        requireNonNull(orgCode);

        final MoosePermitStatisticsI18n i18n = createI18n(locale, speciesCode);

        switch (reportType) {
            case BY_PERMIT:
                return calculateByPermit(i18n, speciesCode, huntingYear, orgType, orgCode).build(groupBy, includeGrandTotal);
            case BY_LOCATION:
                return calculateByLocation(i18n, speciesCode, huntingYear, orgType, orgCode).build(groupBy, includeGrandTotal);
            default:
                throw new IllegalArgumentException("Unknown reportType: " + reportType);
        }
    }

    private MoosePermitStatisticsBuilder calculateByPermit(final MoosePermitStatisticsI18n i18n,
                                                           final int speciesCode,
                                                           final int huntingYear,
                                                           final MoosePermitStatisticsOrganisationType orgType,
                                                           final String orgCode) {
        final Map<Long, MoosePermitStatisticsPermitInfo> permits =
                findPermitsForOrganisation(orgType, orgCode, speciesCode, huntingYear);
        final Set<Long> permitIds = permits.keySet();

        final Map<Long, MoosePermitStatisticsAmountDTO> permitAmountMapping =
                permitAmountService.findPermitAmounts(permitIds, speciesCode, huntingYear);
        final HarvestCountByPermitAndClub harvestCountMapping =
                harvestCountService.countHarvestsGroupingByPermitAndClubId(permitIds, speciesCode);
        final ClubHuntingSummaryBasicInfoByPermitAndClub summaryMapping =
                summaryService.getHuntingSummaries(permitIds, speciesCode);

        final MoosePermitStatisticsBuilder builder = new MoosePermitStatisticsBuilder(i18n);

        for (final Long permitId : permitIds) {
            final MoosePermitStatisticsPermitInfo permitInfo = permits.get(permitId);
            final MoosePermitStatisticsAmountDTO permitAmount = permitAmountMapping.get(permitId);
            final MoosePermitStatisticsAreaAndPopulation areaAndPopulation = MoosePermitStatisticsAreaAndPopulation
                    .create(permitInfo.getPermitAreaSize(), summaryMapping.listByHarvestPermit(permitId));

            builder.addPermit(permitInfo, permitAmount, areaAndPopulation);
            builder.addHarvestCount(new PermitAndLocationId(permitInfo), harvestCountMapping.sumCountsByPermit(permitId));
        }

        return builder;
    }

    private MoosePermitStatisticsBuilder calculateByLocation(final MoosePermitStatisticsI18n i18n,
                                                             final int speciesCode,
                                                             final int huntingYear,
                                                             final MoosePermitStatisticsOrganisationType orgType,
                                                             final String orgCode) {
        final MoosePermitStatisticsBuilder builder = new MoosePermitStatisticsBuilder(i18n);

        final Map<PermitAndLocationId, HarvestCountDTO> harvestByLocation = harvestCountService
                .countHarvestByLocation(speciesCode, huntingYear, orgType, orgCode);
        builder.addHarvestCounts(harvestByLocation);

        final Set<Long> permitIds = F.mapNonNullsToSet(harvestByLocation.keySet(), PermitAndLocationId::getPermitId);

        final Map<Long, MoosePermitStatisticsPermitInfo> permitInfoMapping =
                moosePermitStatisticsListService.findPermits(permitIds, speciesCode, huntingYear);
        final Map<Long, MoosePermitStatisticsAmountDTO> permitAmountMapping =
                permitAmountService.findPermitAmounts(permitIds, speciesCode, huntingYear);
        final ClubHuntingSummaryBasicInfoByPermitAndClub summaryMapping =
                summaryService.getHuntingSummaries(permitIds, speciesCode);

        for (final Long permitId : permitIds) {
            final MoosePermitStatisticsPermitInfo permitInfo = permitInfoMapping.get(permitId);
            final MoosePermitStatisticsAmountDTO permitAmount = permitAmountMapping.get(permitId);
            final MoosePermitStatisticsAreaAndPopulation areaAndPopulation = MoosePermitStatisticsAreaAndPopulation
                    .create(permitInfo.getPermitAreaSize(), summaryMapping.listByHarvestPermit(permitId));
            builder.addPermit(permitInfo, permitAmount, areaAndPopulation);
        }

        return builder;
    }

    private Map<Long, MoosePermitStatisticsPermitInfo> findPermitsForOrganisation(final MoosePermitStatisticsOrganisationType orgType,
                                                                                  final String orgCode,
                                                                                  final int speciesCode,
                                                                                  final int huntingYear) {
        switch (orgType) {
            case RK:
                return moosePermitStatisticsListService.findPermits(speciesCode, huntingYear);
            case RHY:
                return moosePermitStatisticsListService.findPermitsByRhy(orgCode, speciesCode, huntingYear);
            case RKA:
                return moosePermitStatisticsListService.findPermitsByRka(orgCode, speciesCode, huntingYear);
            case HTA:
                return moosePermitStatisticsListService.findPermitsByHta(orgCode, speciesCode, huntingYear);
            default:
                throw new IllegalArgumentException("Unknown orgType: " + orgType);
        }
    }

    private MoosePermitStatisticsI18n createI18n(final Locale locale, final int speciesCode) {
        return new MoosePermitStatisticsI18n(locale,
                gameSpeciesService.getNameIndex().get(speciesCode),
                riistanhoitoyhdistysNameService.getNameIndex(),
                hirvitalousalueService.getNameIndex());
    }
}
