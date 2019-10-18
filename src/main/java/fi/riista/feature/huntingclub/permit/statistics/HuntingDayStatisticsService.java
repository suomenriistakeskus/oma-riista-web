package fi.riista.feature.huntingclub.permit.statistics;

import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
public class HuntingDayStatisticsService {

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Map<Long, HuntingDayStatisticsDTO> calculateStatistics(
            final HarvestPermitSpeciesAmount speciesAmount) {

        // Initialize empty statistics for each partner with moderated hunting summary.
        final Map<Long, HuntingDayStatisticsDTO> results = basicHuntingSummaryRepository
                .findModeratorOverriddenHuntingSummaries(speciesAmount).stream()
                .collect(toMap(s -> s.getClub().getId(), summary -> {
                    final HuntingDayStatisticsDTO dto = new HuntingDayStatisticsDTO();
                    dto.setHuntingClubId(summary.getClub().getId());
                    return dto;
                }));

        final String sql = "SELECT\n" +
                "  daystats.club_id, daystats.day_count, daystats.hunter_count, harveststats.harvest_count,\n" +
                "  observationstats.observation_count,\n" +
                "  daystats.latest_day_update, harveststats.latest_harvest_update, observationstats.latest_observation_update\n" +
                "-- daystats\n" +
                "FROM (SELECT\n" +
                "  days.club_id,\n" +
                "  count(days.group_hunting_day_id) AS day_count,\n" +
                "  max(days.modification_time) AS latest_day_update,\n" +
                "  coalesce(sum(days.number_of_hunters), 0) AS hunter_count\n" +
                "  FROM (SELECT\n" +
                "    clubgroup.parent_organisation_id AS club_id,\n" +
                "    group_hunting_day.modification_time,\n" +
                "    group_hunting_day.group_hunting_day_id,\n" +
                "    group_hunting_day.number_of_hunters\n" +
                getPermitsClubsHuntingDaysJoin() +
                ") AS days\n" +
                "GROUP BY days.club_id\n" +
                ") AS daystats\n" +
                "-- harveststats\n" +
                getHarvestOrObservationStats("harvest", "harvest", "harveststats") +
                "-- observationstats\n" +
                getHarvestOrObservationStats("game_observation", "observation", "observationstats") +

                "ORDER BY daystats.club_id";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("permitId", speciesAmount.getHarvestPermit().getId());
        queryParams.addValue("speciesCode", speciesAmount.getGameSpecies().getOfficialCode());

        jdbcTemplate.query(sql, queryParams, (rs, rowNum) -> {
            final HuntingDayStatisticsDTO dto = new HuntingDayStatisticsDTO();

            dto.setHuntingClubId(rs.getLong("club_id"));

            dto.setLatestUpdate(DateUtil.toLocalDateTimeNullSafe(Stream.of(
                    rs.getTimestamp("latest_day_update"),
                    rs.getTimestamp("latest_harvest_update"),
                    rs.getTimestamp("latest_observation_update")).filter(Objects::nonNull).max(Timestamp::compareTo)
                    .orElse(null)));

            dto.setDayCount(rs.getInt("day_count"));
            dto.setHunterCount(rs.getInt("hunter_count"));
            dto.setHarvestCount(rs.getInt("harvest_count"));
            dto.setObservationCount(rs.getInt("observation_count"));
            return dto;
        }).forEach(dto -> results.putIfAbsent(dto.getHuntingClubId(), dto));

        return results;
    }

    private static String getHarvestOrObservationStats(String table, String statsValueAlias, String statsAlias) {
        return "LEFT JOIN (SELECT\n" +
                " days.club_id AS club_id,\n" +
                " max(item.modification_time) AS latest_" + statsValueAlias + "_update,\n" +
                " coalesce(sum(item.amount), 0) AS " + statsValueAlias + "_count\n" +
                " FROM " + table + " item\n" +
                " JOIN (SELECT\n" +
                "  clubgroup.parent_organisation_id AS club_id,\n" +
                "  group_hunting_day.modification_time,\n" +
                "  group_hunting_day.group_hunting_day_id,\n" +
                "  group_hunting_day.number_of_hunters\n" +
                getPermitsClubsHuntingDaysJoin() +
                ") AS days ON (item.group_hunting_day_id = days.group_hunting_day_id)\n" +
                "  JOIN game_species game_species ON (game_species.game_species_id = item.game_species_id AND game_species.official_code = :speciesCode)\n" +
                "GROUP BY days.club_id) AS " + statsAlias + " ON (daystats.club_id = " + statsAlias + ".club_id)";
    }

    private static String getPermitsClubsHuntingDaysJoin() {
        return " FROM organisation clubgroup\n" +
                " JOIN harvest_permit_species_amount harvest_permit_species_amount ON (\n" +
                "   clubgroup.harvest_permit_id = harvest_permit_species_amount.harvest_permit_id\n" +
                "   AND clubgroup.game_species_id = harvest_permit_species_amount.game_species_id\n" +
                "   AND harvest_permit_species_amount.harvest_permit_id = :permitId)\n" +
                " JOIN game_species game_species ON (game_species.game_species_id = harvest_permit_species_amount.game_species_id AND game_species.official_code = :speciesCode)\n" +
                " JOIN harvest_permit_partners partner ON (partner.harvest_permit_id = :permitId AND partner.organisation_id = clubgroup.parent_organisation_id)\n" +
                " LEFT JOIN group_hunting_day group_hunting_day ON (group_hunting_day.hunting_group_id = clubgroup.organisation_id)";
    }
}

