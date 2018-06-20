package fi.riista.feature.harvestpermit.statistics;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.search.RhySearchParamsFeature.RhySearchOrgType;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

@Component
public class MoosePermitStatisticsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MoosePermitStatisticsService moosePermitStatisticsService;

    @Transactional(readOnly = true)
    public List<MoosePermitStatisticsDTO> calculateByHolder(final long rhyId, final Locale locale, final int speciesCode,
                                                            final int huntingYear, final RhySearchOrgType orgType, final String orgCode) {

        requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);
        return moosePermitStatisticsService.calculateByHolder(locale, speciesCode, huntingYear, orgType, orgCode);
    }

    @Transactional(readOnly = true)
    public List<MoosePermitStatisticsDTO> getRhyStatistics(final long permitId, int speciesCode, Locale locale) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        final int year = permit.getSpeciesAmounts().stream()
                .filter(spa -> spa.getGameSpecies().getOfficialCode() == speciesCode)
                .findAny()
                .map(Has2BeginEndDates::findUnambiguousHuntingYear)
                .orElseGet(OptionalInt::empty)
                .orElseThrow(IllegalStateException::new);

        final Riistanhoitoyhdistys rhy = permit.getRhy();
        return moosePermitStatisticsService.calculateByHolder(locale, speciesCode, year,
                RhySearchOrgType.RHY, rhy.getOfficialCode());
    }
}
