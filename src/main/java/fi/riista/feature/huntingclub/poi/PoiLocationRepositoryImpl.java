package fi.riista.feature.huntingclub.poi;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.huntingclub.area.query.HuntingClubPoiWGS84Query;
import fi.riista.feature.huntingclub.poi.gpx.GpxPoiLocationDTO;
import fi.riista.util.F;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PoiLocationRepositoryImpl implements PoiLocationRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    private HuntingClubPoiWGS84Query wgs84Query;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.wgs84Query = new HuntingClubPoiWGS84Query(dataSource);
    }

    @Transactional(readOnly = true)
    @Override
    public List<GpxPoiLocationDTO> getGpxPointsByPoi(final PoiLocationGroup poi) {
        return doQuery(Collections.singleton(poi));
    }

    @Transactional(readOnly = true)
    @Override
    public List<GpxPoiLocationDTO> getGpxPointsByPoiIn(final Collection<PoiLocationGroup> pois) {
        return doQuery(pois);
    }

    private List<GpxPoiLocationDTO> doQuery(final Collection<PoiLocationGroup> pois) {
        if (pois.isEmpty()) {
            return Collections.emptyList();
        }

        final QPoiLocation POI_LOCATION = QPoiLocation.poiLocation;
        final QPoiLocationGroup POI_GROUP = QPoiLocationGroup.poiLocationGroup;
        final Map<Long, GISWGS84Point> locations = wgs84Query.getLocations(F.getNonNullIds(pois));

        return jpqlQueryFactory
                .select(POI_GROUP.visibleId, POI_GROUP.description, POI_GROUP.type, POI_LOCATION.id,
                        POI_LOCATION.poi.id, POI_LOCATION.visibleId, POI_LOCATION.description)
                .from(POI_LOCATION)
                .innerJoin(POI_LOCATION.poi, POI_GROUP)
                .where(POI_GROUP.in(pois))
                .fetch()
                .stream()
                .map(tuple -> {
                    final GpxPoiLocationDTO dto = new GpxPoiLocationDTO();
                    final Long id = tuple.get(POI_LOCATION.id);
                    final GISWGS84Point point = locations.get(id);

                    dto.setId(id);
                    dto.setPoiId(tuple.get(POI_LOCATION.poi.id));
                    final Integer groupVisibleId = tuple.get(POI_GROUP.visibleId);
                    final Integer locationVisibleId = tuple.get(POI_LOCATION.visibleId);
                    dto.setVisibleId(String.format("%d-%d", groupVisibleId, locationVisibleId));
                    dto.setPoiDescription(tuple.get(POI_GROUP.description));
                    dto.setLocationComment(tuple.get(POI_LOCATION.description));
                    dto.setType(tuple.get(POI_GROUP.type));
                    dto.setLatitude(point.getLatitude());
                    dto.setLongitude(point.getLongitude());
                    return dto;
                }).collect(Collectors.toList());
    }
}
