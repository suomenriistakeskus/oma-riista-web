package fi.riista.feature.huntingclub.permit.statistics;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsOrganisationType;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.QBasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.QModeratedHarvestCounts;
import fi.riista.util.Collect;
import fi.riista.util.F;
import fi.riista.util.JdbcTemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

@Component
public class HarvestCountService {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, HarvestCountDTO> countHarvestsGroupingByClubId(final @Nonnull HarvestPermit permit,
                                                                    final int speciesCode) {
        return countHarvestsGroupingByPermitAndClubId(singleton(permit.getId()), speciesCode).indexByClubId(permit);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public HarvestCountByPermitAndClub countHarvestsGroupingByPermitAndClubId(final Set<Long> permitIds,
                                                                              final int speciesCode) {
        if (permitIds.isEmpty()) {
            return new HarvestCountByPermitAndClub(emptyMap());
        }

        final HashMap<PermitAndClubId, HarvestCountDTO> result = new HashMap<>();
        result.putAll(countClubHarvest(permitIds, speciesCode));
        result.putAll(getModeratedHarvestCounts(permitIds, speciesCode));

        return new HarvestCountByPermitAndClub(result);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<PermitAndClubId, HarvestCountDTO> getModeratedHarvestCounts(final Set<Long> permitIds,
                                                                           final int speciesCode) {
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QBasicClubHuntingSummary BASIC_SUMMARY = QBasicClubHuntingSummary.basicClubHuntingSummary;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        final NumberPath<Long> clubId = BASIC_SUMMARY.club.id;
        final NumberPath<Long> permitId = SPECIES_AMOUNT.harvestPermit.id;
        final QModeratedHarvestCounts mapValue = BASIC_SUMMARY.moderatedHarvestCounts;

        return jpqlQueryFactory
                .select(permitId, clubId, mapValue)
                .from(BASIC_SUMMARY)
                .join(BASIC_SUMMARY.speciesAmount, SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .where(SPECIES_AMOUNT.harvestPermit.id.in(permitIds))
                .where(SPECIES.officialCode.eq(speciesCode))
                .where(BASIC_SUMMARY.moderatorOverride.isTrue())
                .fetch().stream()
                .collect(Collectors.toMap(
                        tuple -> new PermitAndClubId(tuple.get(permitId), tuple.get(clubId)),
                        tuple -> new HarvestCountDTO(tuple.get(mapValue))));
    }

    @Nonnull
    private Map<PermitAndClubId, HarvestCountDTO> countClubHarvest(final Set<Long> permitIds, final int speciesCode) {
        final String sql = "SELECT hp.harvest_permit_id AS permit_id, pp.organisation_id AS club_id," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'MALE')   THEN 1 END), 0) AS adult_males," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'FEMALE') THEN 1 END), 0) AS adult_females," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'MALE')   THEN 1 END), 0) AS young_males," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'FEMALE') THEN 1 END), 0) AS young_females," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'MALE'   AND hs.not_edible = TRUE) THEN 1 END), 0) AS adult_males_not_edible," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'FEMALE' AND hs.not_edible = TRUE) THEN 1 END), 0) AS adult_females_not_edible," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'MALE'   AND hs.not_edible = TRUE) THEN 1 END), 0) AS young_males_not_edible," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'FEMALE' AND hs.not_edible = TRUE) THEN 1 END), 0) AS young_females_not_edible " +
                " FROM harvest_permit hp" +
                "  JOIN harvest_permit_species_amount hpsa ON (hpsa.harvest_permit_id = hp.harvest_permit_id)" +
                "  JOIN game_species g ON (g.game_species_id = hpsa.game_species_id)" +
                "  JOIN harvest_permit_partners pp ON (pp.harvest_permit_id = hp.harvest_permit_id)" +
                "  LEFT JOIN organisation clubgroup ON (clubgroup.harvest_permit_id = hp.harvest_permit_id" +
                "    AND clubgroup.parent_organisation_id = pp.organisation_id" +
                "    AND clubgroup.game_species_id = hpsa.game_species_id" +
                "    AND clubgroup.organisation_type = 'CLUBGROUP')" +
                "  LEFT JOIN group_hunting_day ghd ON (ghd.hunting_group_id = clubgroup.organisation_id)" +
                "  LEFT JOIN harvest h ON (h.group_hunting_day_id = ghd.group_hunting_day_id" +
                "    AND h.game_species_id = hpsa.game_species_id)" +
                "  LEFT JOIN harvest_specimen hs ON (hs.harvest_id = h.harvest_id)" +
                " WHERE hp.harvest_permit_id IN (:permitIds)" +
                " AND g.official_code = :speciesCode" +
                " GROUP BY hp.harvest_permit_id, pp.organisation_id;";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource()
                .addValue("permitIds", permitIds)
                .addValue("speciesCode", speciesCode);

        return jdbcTemplate.query(sql, queryParams, (rs, rowNum) -> {
            final PermitAndClubId key = new PermitAndClubId(
                    rs.getLong("permit_id"),
                    rs.getLong("club_id"));

            final HarvestCountDTO count = new HarvestCountDTO(
                    rs.getInt("adult_males"),
                    rs.getInt("adult_females"),
                    rs.getInt("young_males"),
                    rs.getInt("young_females"),
                    rs.getInt("adult_males_not_edible"),
                    rs.getInt("adult_females_not_edible"),
                    rs.getInt("young_males_not_edible"),
                    rs.getInt("young_females_not_edible"));
            return F.entry(key, count);
        }).stream().collect(Collect.entriesToMap());
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<PermitAndLocationId, HarvestCountDTO> countHarvestByLocation(final int speciesCode, final int huntingYear,
                                                                            final MoosePermitStatisticsOrganisationType orgType,
                                                                            final String orgCode) {
        Preconditions.checkArgument(StringUtils.hasText(orgCode));

        final String filterSql;

        switch (orgType) {
            case RK:
                filterSql = "1 = 1";
                break;
            case RHY:
                filterSql = "rhy.organisation_id IN (SELECT r.organisation_id FROM organisation r WHERE r.organisation_type = 'RHY' AND r.official_code = '" + orgCode + "')";
                break;
            case RKA:
                filterSql = "rka.organisation_id IN (SELECT r.organisation_id FROM organisation r WHERE r.organisation_type = 'RKA' AND r.official_code = '" + orgCode + "')";
                break;
            case HTA:
                filterSql = "hta.numero = '" + orgCode + "'";
                break;
            default:
                throw new IllegalArgumentException("Invalid orgType " + orgType);
        }

        final String sql = "SELECT" +
                "  clubgroup.harvest_permit_id AS permit_id," +
                "  rhy.organisation_id AS rhy_id," +
                "  rka.organisation_id AS rka_id," +
                "  hta.gid AS hta_id, " +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'MALE')   THEN 1 END), 0) AS adult_males," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'FEMALE') THEN 1 END), 0) AS adult_females," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'MALE')   THEN 1 END), 0) AS young_males," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'FEMALE') THEN 1 END), 0) AS young_females," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'MALE'   AND hs.not_edible = TRUE) THEN 1 END), 0) AS adult_males_not_edible," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'ADULT' AND hs.gender = 'FEMALE' AND hs.not_edible = TRUE) THEN 1 END), 0) AS adult_females_not_edible," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'MALE'   AND hs.not_edible = TRUE) THEN 1 END), 0) AS young_males_not_edible," +
                "  COALESCE(SUM(CASE WHEN (hs.age = 'YOUNG' AND hs.gender = 'FEMALE' AND hs.not_edible = TRUE) THEN 1 END), 0) AS young_females_not_edible " +
                " FROM organisation clubgroup" +
                "  JOIN group_hunting_day ghd ON (clubgroup.organisation_id = ghd.hunting_group_id)" +
                "  JOIN harvest h ON (ghd.group_hunting_day_id = h.group_hunting_day_id)" +
                "  JOIN organisation rhy ON (rhy.organisation_id = h.rhy_id)" +
                "  JOIN organisation rka ON (rka.organisation_id = rhy.parent_organisation_id)" +
                "  LEFT JOIN harvest_specimen hs ON (hs.harvest_id = h.harvest_id)" +
                "  LEFT JOIN hta ON ST_Intersects(hta.geom, h.geom)" +
                " WHERE clubgroup.organisation_type = 'CLUBGROUP'" +
                "   AND clubgroup.hunting_year = :huntingYear" +
                "   AND clubgroup.game_species_id = (SELECT game_species_id FROM game_species WHERE official_code = :speciesCode)" +
                "   AND clubgroup.harvest_permit_id IS NOT NULL" +
                "   AND " + filterSql +
                " GROUP BY clubgroup.harvest_permit_id, rka.organisation_id, rhy.organisation_id, hta.gid";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource()
                .addValue("speciesCode", speciesCode)
                .addValue("huntingYear", huntingYear);

        return jdbcTemplate.query(sql, queryParams, (rs, rowNum) -> {
            final HarvestCountDTO count = new HarvestCountDTO(
                    rs.getInt("adult_males"),
                    rs.getInt("adult_females"),
                    rs.getInt("young_males"),
                    rs.getInt("young_females"),
                    rs.getInt("adult_males_not_edible"),
                    rs.getInt("adult_females_not_edible"),
                    rs.getInt("young_males_not_edible"),
                    rs.getInt("young_females_not_edible"));

            final long permitId = rs.getLong("permit_id");
            final Long rhyId = JdbcTemplateUtils.getLongOrNull(rs, "rhy_id");
            final Long rkaId = JdbcTemplateUtils.getLongOrNull(rs, "rka_id");
            final Integer htaId = JdbcTemplateUtils.getIntegerOrNull(rs, "hta_id");

            final PermitAndLocationId key = new PermitAndLocationId(rhyId, rkaId, htaId, permitId);

            return F.entry(key, count);
        }).stream().collect(Collect.entriesToMap());
    }
}
