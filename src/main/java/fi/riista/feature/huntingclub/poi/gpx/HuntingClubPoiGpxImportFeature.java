package fi.riista.feature.huntingclub.poi.gpx;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.poi.PoiLocationDTO;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class HuntingClubPoiGpxImportFeature {

    private NamedParameterJdbcOperations namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public List<PoiLocationDTO> convertGpxPoints(final MultipartFile importedFile) {
        try {
            final GPX parsedGpx = GPX.read(importedFile.getInputStream());
            return parsedGpx.wayPoints()
                    .map(this::toPoiLocationDTO)
                    .collect(toList());
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private PoiLocationDTO toPoiLocationDTO(final WayPoint wp) {
        final PoiLocationDTO dto = new PoiLocationDTO();
        dto.setGeoLocation(wayPointToGeoLocation(wp));
        dto.setDescription(wp.getName().orElse(wp.getDescription().orElse(null)));
        return dto;
    }

    private GeoLocation wayPointToGeoLocation(final WayPoint wp) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lon", wp.getLongitude().doubleValue())
                .addValue("lat", wp.getLatitude().doubleValue());

        // TODO: more efficiency, maybe
        final String sql =
                "WITH point AS ( " +
                        "SELECT ST_Transform(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), 3067) AS geom" +
                        ") SELECT ST_X(point.geom) AS longitude, ST_Y(point.geom) AS latitude " +
                        "FROM point";

        return namedParameterJdbcTemplate.query(sql, params, (rs, i) ->
                new GeoLocation((int) Math.round(rs.getDouble("latitude")),
                                (int) Math.round(rs.getDouble("longitude")))).get(0);
    }

}
