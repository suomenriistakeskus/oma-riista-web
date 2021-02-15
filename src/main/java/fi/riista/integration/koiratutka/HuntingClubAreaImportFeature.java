package fi.riista.integration.koiratutka;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.geojson.FeatureCollectionWithProperties;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.Patterns;
import fi.riista.util.PolygonConversionUtil;
import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class HuntingClubAreaImportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HuntingClubAreaImportFeature.class);

    private static final Pattern PROPERTY_IDENTIFIER_DELIMITED_REGEX =
            Pattern.compile(Patterns.PROPERTY_IDENTIFIER_DELIMITED);

    private static final Pattern PROPERTY_IDENTIFIER_NORMALISED_REGEX =
            Pattern.compile(Patterns.PROPERTY_IDENTIFIER_NORMALISED);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional
    public void importGeojson(final long huntingClubAreaId,
                              final FeatureCollectionWithProperties featureCollection) {
        final GISUtils.SRID srid = extractSrid(featureCollection);

        if (srid == null) {
            throw new IllegalArgumentException("GeoJSON CRS is not supported");
        }

        final HuntingClubArea clubArea = requireEntityService.requireHuntingClubArea(
                huntingClubAreaId, EntityPermission.UPDATE);

        final GISZone zone = Optional.ofNullable(clubArea.getZone())
                .orElseGet(() -> new GISZone(GISZone.SourceType.EXTERNAL));
        zone.setSourceType(GISZone.SourceType.EXTERNAL);
        zone.setExcludedGeom(null);
        zone.setComputedAreaSize(-1);
        zone.setWaterAreaSize(-1);

        // Make sure modification time is updated
        zone.setModificationTimeToCurrentTime();
        clubArea.setModificationTimeToCurrentTime();

        final List<HuntingClubAreaImportFeatureDTO> dtoList = featureCollection.getFeatures().stream()
                .map(feature -> {
                    if (GeoJSONConstants.ID_EXCLUDED.equals(feature.getId())) {
                        if (feature.getGeometry() != null) {
                            final Geometry excludedGeom = PolygonConversionUtil.geoJsonToJava(
                                    feature.getGeometry(), srid);

                            zone.setExcludedGeom(excludedGeom);
                        }
                        return Optional.<HuntingClubAreaImportFeatureDTO> empty();
                    }
                    return processFeature(feature, srid);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        clubArea.setZone(zone);

        zoneRepository.saveAndFlush(zone);
        zoneRepository.updateExternalFeatures(zone.getId(), srid, dtoList);
        zoneRepository.calculateCombinedGeometry(zone.getId());
        zoneRepository.calculateAreaSize(zone.getId(), false);
    }

    private static Optional<HuntingClubAreaImportFeatureDTO> processFeature(final Feature feature,
                                                                            final GISUtils.SRID srid) {
        final HuntingClubAreaImportFeatureDTO dto = new HuntingClubAreaImportFeatureDTO();

        feature.getProperties().forEach((key, value) -> {
            if (key != null && value != null) {
                if (GeoJSONConstants.PROPERTY_NUMBER.equals(key)) {
                    dto.setPropertyIdentifier(normalizePropertyIdentifier(value.toString()));
                } else if ("prey".equals(key)) {
                    dto.setValidSpecies(parseSpeciesCodes(value));
                } else {
                    LOG.info("Unknown property key={} value={} class={}", key, value, value.getClass().getName());
                }
            }
        });

        if (dto.getPropertyIdentifier() == null) {
            LOG.warn("Ignoring feature without property identifier");
            return Optional.empty();
        }

        if (feature.getGeometry() == null) {
            LOG.warn("Skipping feature with missing geometry: propertyIdentifier={}", dto.getPropertyIdentifier());
            return Optional.empty();
        }

        final Geometry geometry = PolygonConversionUtil.geoJsonToJava(feature.getGeometry(), srid);

        if (geometry.isEmpty()) {
            LOG.warn("Skipping feature with empty geometry: propertyIdentifier={}", dto.getPropertyIdentifier());
            return Optional.empty();
        }

        if (geometry.isValid()) {
            dto.setGeometry(geometry);
        } else {
            LOG.warn("Trying to fix feature with invalid geometry: propertyIdentifier={}", dto.getPropertyIdentifier());

            final Geometry fixedGeometry = geometry.buffer(0);

            if (fixedGeometry.isValid()) {
                dto.setGeometry(fixedGeometry);
            } else {
                LOG.warn("Could not fix geometry: propertyIdentifier={}", dto.getPropertyIdentifier());
                return Optional.empty();
            }
        }

        if (dto.getValidSpecies() == null) {
            dto.setValidSpecies(Collections.emptySet());

        } else if (!dto.getValidSpecies().isEmpty() && !dto.getValidSpecies().contains(GameSpecies.OFFICIAL_CODE_MOOSE)) {
            LOG.info("Skipping non moose propertyIdentifier={}", dto.getPropertyIdentifier());
            return Optional.empty();
        }

        return Optional.of(dto);
    }

    private static GISUtils.SRID extractSrid(final FeatureCollection featureCollection) {
        return Optional.ofNullable(featureCollection.getCrs())
                .map(GISUtils.SRID::fromGeoJsonCrs)
                .orElse(GISUtils.SRID.ETRS_TM35FIN);
    }

    private static Set<Integer> parseSpeciesCodes(final @Nonnull Object propertyValue) {
        if (propertyValue instanceof String) {
            return "all".equals(propertyValue)
                    ? Collections.emptySet()
                    : Collections.singleton(Integer.valueOf(propertyValue.toString()));
        }

        if (Iterable.class.isAssignableFrom(propertyValue.getClass())) {
            return parseSpeciesCodesCollection(Iterable.class.cast(propertyValue));
        }

        throw new IllegalArgumentException("Invalid value for prey: " + propertyValue);
    }

    private static Set<Integer> parseSpeciesCodesCollection(final @Nonnull Iterable<?> iterable) {
        return F.stream(iterable)
                .filter(Objects::nonNull)
                .map(v -> {
                    if (v instanceof Number) {
                        return Number.class.cast(v).intValue();
                    } else if (v instanceof String) {
                        return Integer.parseInt(v.toString());
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.toSet());
    }

    private static String normalizePropertyIdentifier(final @Nonnull String propertyIdentifier) {
        final Matcher dm = PROPERTY_IDENTIFIER_DELIMITED_REGEX.matcher(propertyIdentifier);

        if (dm.matches()) {
            return StringUtils.leftPad(dm.group(1), 3, '0') +
                    StringUtils.leftPad(dm.group(2), 3, '0') +
                    StringUtils.leftPad(dm.group(3), 4, '0') +
                    StringUtils.leftPad(dm.group(4), 4, '0');
        }

        final Matcher nm = PROPERTY_IDENTIFIER_NORMALISED_REGEX.matcher(propertyIdentifier);

        if (nm.matches()) {
            return propertyIdentifier;
        }

        throw new IllegalArgumentException("Invalid propertyIdentifier=" + propertyIdentifier);
    }
}
