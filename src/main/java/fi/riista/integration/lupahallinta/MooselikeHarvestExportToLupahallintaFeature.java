package fi.riista.integration.lupahallinta;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsCount;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsDTO;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsService;
import fi.riista.integration.lupahallinta.club.LHMooselikeHarvestsCSVRow;
import fi.riista.util.Locales;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class MooselikeHarvestExportToLupahallintaFeature {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private MoosePermitStatisticsService moosePermitStatisticsService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_MOOSELIKE_HARVESTS')")
    public List<LHMooselikeHarvestsCSVRow> exportToCSV(final int huntingYear) {
        return GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.stream()
                .flatMap(speciesCode -> findCsvRowsForSpecies(huntingYear, speciesCode))
                .collect(toList());
    }

    private Stream<LHMooselikeHarvestsCSVRow> findCsvRowsForSpecies(final int huntingYear, final Integer speciesCode) {
        final List<HarvestPermit> permits = findPermits(huntingYear, speciesCode);
        return moosePermitStatisticsService.calculateByPartner(Locales.FI, speciesCode, huntingYear, permits)
                .stream()
                .map(dto -> createCsvRow(speciesCode, dto));
    }

    private static LHMooselikeHarvestsCSVRow createCsvRow(final Integer speciesCode,
                                                          final MoosePermitStatisticsDTO dto) {

        final MoosePermitStatisticsCount count = dto.getHarvestCount();
        return new LHMooselikeHarvestsCSVRow(
                dto.getPermitNumber(),
                dto.getPermitHolderOfficialCode(),
                speciesCode,
                count.getAdultMales(),
                count.getAdultFemales(),
                count.getYoungMales(),
                count.getYoungFemales(),
                count.getAdultsNonEdible(),
                count.getYoungNonEdible(),
                count.getTotalAreaSize(),
                count.getEffectiveAreaSize(),
                count.getRemainingPopulationInTotalArea(),
                count.getRemainingPopulationInEffectiveArea()
        );
    }

    private List<HarvestPermit> findPermits(final int huntingYear, final Integer speciesCode) {
        final QHarvestPermit harvestPermit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies gameSpecies = QGameSpecies.gameSpecies;
        return queryFactory.selectFrom(harvestPermit)
                .join(harvestPermit.speciesAmounts, speciesAmount)
                .join(speciesAmount.gameSpecies, gameSpecies)
                .where(harvestPermit.isMooselikePermit(),
                        speciesAmount.validOnHuntingYear(huntingYear),
                        gameSpecies.officialCode.eq(speciesCode)
                ).fetch();
    }
}
