package fi.riista.feature.harvestpermit.allocation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.util.LocalisedString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

@Transactional
@Repository
public class HarvestPermitAllocationRepositoryImpl implements HarvestPermitAllocationRepositoryCustom {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public double countAllocatedPermitCount(final HarvestPermit permit, final GameSpecies species) {
        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("permitId", permit.getId());
        queryParams.addValue("speciesId", species.getId());

        return jdbcTemplate.queryForObject("SELECT COALESCE(SUM(adult_males + adult_females + young / 2.0), 0)" +
                " FROM harvest_permit_allocation a" +
                " WHERE a.harvest_permit_id = :permitId AND a.game_species_id = :speciesId", queryParams, double.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoosePermitAllocationDTO> getAllocationsIncludeMissingPartnerDTO(final HarvestPermit permit, final GameSpecies species) {
        Objects.requireNonNull(permit, "permit is null");
        Objects.requireNonNull(species, "species is null");

        // Query is optimized by explicitly guiding execution with the inner query (producing
        // derived table) to narrow down the result set by selecting exactly one row from
        // the harvest_permit table (using primary key index) and then joining only a few rows
        // from other tables. Because harvest_permit table can eventually become quite large, we
        // can take it granted that restricting harvest_permit rows (straight from the beginning)
        // is the most effective way to limit the number of rows database needs to scan while
        // executing the query.

        final String sql = "SELECT club.organisation_id, club.name_finnish, club.name_swedish, \n" +
                "  hpa.total, hpa.adult_males, hpa.adult_females, hpa.young \n" +
                "FROM ( \n" +
                "  SELECT harvest_permit_id AS permit_id, organisation_id AS club_id \n" +
                "  FROM harvest_permit_partners \n" +
                "  WHERE harvest_permit_id = :permitId \n" +
                ") t \n" +
                "  JOIN organisation club ON club.organisation_id = t.club_id \n" +
                "  JOIN harvest_permit_species_amount hpsa ON hpsa.harvest_permit_id = t.permit_id \n" +
                "  JOIN game_species g ON \n" +
                "    (g.game_species_id = hpsa.game_species_id AND g.official_code = :speciesCode) \n" +
                "  LEFT JOIN harvest_permit_allocation hpa ON \n" +
                "    (hpa.harvest_permit_id = t.permit_id \n" +
                "     AND hpa.game_species_id = g.game_species_id \n" +
                "     AND hpa.hunting_club_id = t.club_id) \n" +
                "ORDER BY club.organisation_id";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("permitId", permit.getId());
        queryParams.addValue("speciesCode", species.getOfficialCode());

        return jdbcTemplate.query(sql, queryParams, (rs, rowNum) -> {
            final MoosePermitAllocationDTO dto = new MoosePermitAllocationDTO();
            dto.setHuntingClubId(rs.getLong("organisation_id"));
            dto.setHuntingClubName(
                    LocalisedString.of(rs.getString("name_finnish"), rs.getString("name_swedish")).asMap());

            dto.setTotal(rs.getFloat("total"));
            dto.setAdultMales(rs.getInt("adult_males"));
            dto.setAdultFemales(rs.getInt("adult_females"));
            dto.setYoung(rs.getInt("young"));
            return dto;
        });
    }
}
