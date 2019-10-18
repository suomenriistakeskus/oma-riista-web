package fi.riista.feature.organization.rhy.annualstats.export;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.QRhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.getOfficialCodesOfRhysNotExistingAtYear;
import static fi.riista.util.Collect.indexingByIdOf;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class AnnualStatisticsExportService {

    private static final Comparator<AnnualStatisticsExportDTO> DTO_COMPARATOR =
            comparing(dto -> dto.getOrganisation().getOfficialCode());

    @Resource
    private RhyAnnualStatisticsRepository statisticsRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<AnnualStatisticsExportDTO> exportAnnualStatistics(final int calendarYear) {

        final Map<Long, RhyAnnualStatistics> indexByRhyId = statisticsRepository
                .findByYear(calendarYear)
                .stream()
                .collect(indexingByIdOf(RhyAnnualStatistics::getRhy));

        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        return jpqlQueryFactory
                .selectFrom(RHY)
                .innerJoin(RHY.parentOrganisation).fetchJoin()
                .where(RHY.officialCode.notIn(getOfficialCodesOfRhysNotExistingAtYear(calendarYear)))
                .fetch()
                .stream()
                .map(rhy -> {
                    final RhyAnnualStatistics statistics =
                            indexByRhyId.computeIfAbsent(rhy.getId(), id -> new RhyAnnualStatistics(rhy, calendarYear));

                    return export(statistics, rhy, rhy.getParentOrganisation());
                })
                .sorted(DTO_COMPARATOR)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<AnnualStatisticsExportDTO> exportAnnualStatistics(final int calendarYear,
                                                                  @Nonnull final EnumSet<RhyAnnualStatisticsState> states) {

        checkArgument(!F.isNullOrEmpty(states), "states must not be empty or null");

        final QRhyAnnualStatistics STATS = QRhyAnnualStatistics.rhyAnnualStatistics;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = QOrganisation.organisation;

        return jpqlQueryFactory
                .selectFrom(STATS)
                .innerJoin(STATS.rhy, RHY).fetchJoin()
                .innerJoin(RHY.parentOrganisation, RKA).fetchJoin()
                .where(STATS.year.eq(calendarYear), STATS.state.in(states))
                .fetch()
                .stream()
                .map(this::export)
                .sorted(DTO_COMPARATOR)
                .collect(toList());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public AnnualStatisticsExportDTO export(@Nonnull final RhyAnnualStatistics annualStatistics) {
        requireNonNull(annualStatistics);

        final Riistanhoitoyhdistys rhy = annualStatistics.getRhy();

        return export(annualStatistics, rhy, rhy.getParentOrganisation());
    }

    private static AnnualStatisticsExportDTO export(final RhyAnnualStatistics annualStatistics,
                                                    final Riistanhoitoyhdistys rhy,
                                                    final Organisation rka) {
        return AnnualStatisticsExportDTO.create(
                OrganisationNameDTO.createWithOfficialCode(rhy),
                OrganisationNameDTO.createWithOfficialCode(rka),
                annualStatistics);
    }
}
