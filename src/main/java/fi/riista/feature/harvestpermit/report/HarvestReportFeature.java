package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsRepository;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields_;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestQuotaRepository;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonRepository;
import fi.riista.feature.harvestpermit.season.HarvestSeason_;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;

import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.isNotNull;
import static fi.riista.util.jpa.JpaSpecs.or;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class HarvestReportFeature {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestReportFieldsRepository harvestReportFieldsRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Transactional(readOnly = true)
    public List<HarvestSeasonDTO> listReportableHuntingSeasons(
            @Nullable LocalDate date, @Nullable Integer gameSpeciesCode) {
        return F.mapNonNullsToList(getHarvestSeasons(date, gameSpeciesCode), HarvestSeasonDTO::create);
    }

    private List<HarvestSeason> getHarvestSeasons(
            @Nullable final LocalDate date, @Nullable final Integer speciesCode) {

        final Specification<HarvestSeason> withSpeciesCode = speciesCode == null
                ? JpaSpecs.conjunction()
                : JpaSpecs.equal(
                HarvestSeason_.fields, HarvestReportFields_.species, GameSpecies_.officialCode, speciesCode);

        if (date == null) {
            return harvestSeasonRepository.findAll(withSpeciesCode);
        }

        final Specification<HarvestSeason> interval1 =
                JpaSpecs.withinInterval(HarvestSeason_.beginDate, HarvestSeason_.endOfReportingDate, date);
        final Specification<HarvestSeason> interval2 =
                JpaSpecs.withinInterval(HarvestSeason_.beginDate2, HarvestSeason_.endOfReportingDate2, date);

        return harvestSeasonRepository.findAll(where(withSpeciesCode).and(or(
                interval1, and(isNotNull(HarvestSeason_.beginDate2), interval2))));
    }

    @Transactional(readOnly = true)
    public List<HarvestReportFieldsDTO> listReportableWithPermits(
            @Nullable LocalDate date, @Nullable Integer gameSpeciesCode) {
        return F.mapNonNullsToList(getHarvestReportFieldses(date, gameSpeciesCode), HarvestReportFieldsDTO::create);
    }

    private List<HarvestReportFields> getHarvestReportFieldses(
            @Nullable final LocalDate date, @Nullable final Integer gameSpeciesCode) {

        final Specification<HarvestReportFields> withSpeciesCode = gameSpeciesCode == null
                ? JpaSpecs.conjunction()
                : JpaSpecs.equal(HarvestReportFields_.species, GameSpecies_.officialCode, gameSpeciesCode);

        Specifications<HarvestReportFields> spec =
                where(JpaSpecs.equal(HarvestReportFields_.usedWithPermit, true))
                        .and(withSpeciesCode);

        if (date != null) {
            spec = spec.and(
                    JpaSpecs.withinInterval(HarvestReportFields_.beginDate, HarvestReportFields_.endDate, date));
        }

        return harvestReportFieldsRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public HarvestAreaDTO findHarvestArea(long rhyId, long harvestSeasonId) {
        final HarvestQuota quota = harvestQuotaRepository.findByHarvestSeasonAndRhy(harvestSeasonId, rhyId);

        return quota == null ? null : HarvestAreaDTO.create(quota.getHarvestArea());
    }

}
