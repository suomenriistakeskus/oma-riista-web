package fi.riista.feature.huntingclub.poi.gpx;

import fi.riista.config.Constants;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationGroup;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationRepository;
import fi.riista.feature.huntingclub.poi.PointOfInterestService;
import fi.riista.security.EntityPermission;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Longitude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;

@Service
public class HuntingClubPoiGpxExportFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PointOfInterestService poiService;

    @Resource
    private PoiLocationGroupRepository locationGroupRepository;

    @Resource
    private HuntingClubAreaRepository areaRepository;

    @Resource
    private PoiLocationRepository repository;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportByClub(final long clubId, final HttpServletResponse response, final Locale locale) throws IOException {
        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        final List<PoiLocationGroup> locationGroups = poiService.listForClub(huntingClub);
        final List<GpxPoiLocationDTO> gpxPoints = repository.getGpxPointsByPoiIn(locationGroups);

        final String filenamePrefix = enumLocaliser.getTranslation("PointOfInterest.fileName");
        final String localisedClubName = enumLocaliser.getTranslation(huntingClub.getNameLocalisation());
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());

        final String filename = String.format("%s-%s-%s.gpx", filenamePrefix, localisedClubName, timestamp);
        ContentDispositionUtil.addHeader(response, filename);

        doExport(response.getOutputStream(), locale, huntingClub, gpxPoints);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public void exportByArea(final long areaId, HttpServletResponse response, final Locale locale) throws IOException {
        final HuntingClubArea area = requireEntityService.requireHuntingClubArea(areaId, EntityPermission.READ);
        final List<Long> poiIds = areaRepository.listPois(area.getId());

        final List<PoiLocationGroup> pois = locationGroupRepository.findAllById(poiIds);
        final List<GpxPoiLocationDTO> gpxPoints = repository.getGpxPointsByPoiIn(pois);

        final String filenamePrefix = enumLocaliser.getTranslation("PointOfInterest.fileName");
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        
        final String filename = String.format("%s-%s-%s.gpx", filenamePrefix, area.getExternalId(), timestamp);
        ContentDispositionUtil.addHeader(response, filename);

        doExport(response.getOutputStream(), locale, area.getClub(), gpxPoints);
    }

    private void doExport(final OutputStream os, final Locale locale, final HuntingClub huntingClub,
                          final List<GpxPoiLocationDTO> gpxPoints) throws IOException {
        final GPX.Builder builder = GPX.builder(huntingClub.getNameLocalisation().getAnyTranslation(locale));

        gpxPoints.stream()
                .sorted(Comparator.comparing(GpxPoiLocationDTO::getVisibleId))
                .forEach(point ->
                        builder.addWayPoint(wp ->
                                wp.name(waypointName(point))
                                        .desc(point.getLocationComment())
                                        .build(Latitude.ofDegrees(point.getLatitude()), Longitude.ofDegrees(point.getLongitude()))
                        )
                );

        final GPX gpx = builder.build();

        GPX.write(gpx, os);
    }

    private String waypointName(final GpxPoiLocationDTO point) {
        final String type = enumLocaliser.getTranslation(point.getType());
        return ofNullable(point.getPoiDescription())
                .map(d -> String.format("%s (%s) %s", point.getVisibleId(), type, d))
                .orElseGet(() ->
                        String.format("%s (%s)", point.getVisibleId(), type));
    }
}
