package fi.riista.feature.organization.rhy.huntingcontrolevent;

import com.google.common.collect.Sets;
import fi.riista.util.F;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HuntingControlEventQuery {

    private final NamedParameterJdbcTemplate jdbcOperations;

    public HuntingControlEventQuery(final NamedParameterJdbcTemplate jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public Map<Long, Set<Long>> mapInspectorPersonIdsByEventId(final Collection<HuntingControlEvent> events) {
        final String sql =
                "SELECT hunting_control_event_id, person_id " +
                "FROM hunting_control_event_inspector " +
                "WHERE hunting_control_event_id in (:ids)";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("ids", F.getNonNullIds(events));

        final Map<Long, Set<Long>> results = new HashMap<>();

        jdbcOperations.query(sql, queryParams, rs -> {
            final Long eventId = rs.getLong("hunting_control_event_id");
            final Long personId = rs.getLong("person_id");

            if (results.containsKey(eventId)) {
                results.get(eventId).add(personId);
            } else {
                results.put(eventId, Sets.newHashSet(personId));
            }
        });

        return results;
    }

    public Map<Long, Set<HuntingControlCooperationType>> mapCooperationTypesByEventId(final Collection<HuntingControlEvent> events) {
        final String sql =
                "SELECT hunting_control_event_id, type " +
                "FROM hunting_control_cooperation " +
                "WHERE hunting_control_event_id in (:ids)";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("ids", F.getNonNullIds(events));

        final Map<Long, Set<HuntingControlCooperationType>> results = new HashMap<>();

        jdbcOperations.query(sql, queryParams, rs -> {
            final Long eventId = rs.getLong("hunting_control_event_id");
            final HuntingControlCooperationType type = HuntingControlCooperationType.valueOf(rs.getString("type"));

            if (results.containsKey(eventId)) {
                results.get(eventId).add(type);
            } else {
                results.put(eventId, Sets.newHashSet(type));
            }
        });

        return results;
    }

}
