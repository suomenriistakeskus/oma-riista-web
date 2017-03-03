package fi.riista.integration.lupahallinta.permitarea;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.AreaEntity;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.harvestpermit.area.HarvestPermitArea;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaEventRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaHta;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaPartner;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaPartnerRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaRhy;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.squareMetersToHectares;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class LHHarvestPermitAreaFeature {

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private HarvestPermitAreaPartnerRepository harvestPermitAreaPartnerRepository;

    @Resource
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource(name = "lupaHallintaPermitAreaExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Transactional
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_PERMIT_AREA')")
    public void updateLockedStatus(final String externalId, final boolean isLocked) {
        final HarvestPermitArea area = getByExternalIdInternal(externalId);

        (isLocked ? area.setStatusLocked() : area.setStatusUnlocked())
                .ifPresent(harvestPermitAreaEventRepository::save);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_PERMIT_AREA')")
    public LHPA_PermitArea getByExternalId(final String externalId) {
        return toXmlPermitArea(getByExternalIdInternal(externalId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_PERMIT_AREA')")
    public String getByExternalIdXml(final String externalId) {
        return JaxbUtils.marshalToString(getByExternalId(externalId), jaxbMarshaller);
    }

    @Nonnull
    private LHPA_PermitArea toXmlPermitArea(final HarvestPermitArea area) {
        final List<HarvestPermitAreaPartner> partners =
                harvestPermitAreaPartnerRepository.findByHarvestPermitArea(area);
        final Function<AreaEntity<Long>, GISZoneWithoutGeometryDTO> zoneFn =
                createZoneFunction(F.stream(area, partners));

        final LHPA_PermitArea xml = new LHPA_PermitArea();

        xml.setNameFinnish(area.getNameFinnish());
        xml.setNameSwedish(area.getNameSwedish());
        xml.setOfficialCode(area.getExternalId());
        xml.setState(toXmlState(area.getStatus()));
        xml.setLastModified(DateUtil.toLocalDateTimeNullSafe(area.getLifecycleFields().getModificationTime()));

        final GISZoneWithoutGeometryDTO zone = zoneFn.apply(area);
        xml.setTotalAreaSize(squareMetersToHectares(zone.getComputedAreaSize()));
        xml.setWaterAreaSize(squareMetersToHectares(zone.getWaterAreaSize()));

        xml.setRhy(toXmlRhy(area.getRhy()));
        xml.setHta(toXmlHta(area.getHta()));
        xml.setPartners(toXmlPartner(partners, zoneFn));

        return xml;
    }

    private Function<AreaEntity<Long>, GISZoneWithoutGeometryDTO> createZoneFunction(
            final Stream<AreaEntity<Long>> areaEntities) {

        final Function<AreaEntity<Long>, Long> areaToZoneId = area -> area.getZone().getId();
        final Map<Long, GISZoneWithoutGeometryDTO> zoneIndex =
                F.indexById(gisZoneRepository.fetchWithoutGeometry(areaEntities.map(areaToZoneId).collect(toSet())));

        return areaToZoneId.andThen(zoneIndex::get);
    }

    private static List<LHPA_NameWithOfficialCode> toXmlHta(final Collection<HarvestPermitAreaHta> input) {
        return input.stream()
                .sorted(comparing(HarvestPermitAreaHta::getAreaSize).reversed())
                .map(LHHarvestPermitAreaFeature::toXmlHta)
                .collect(toList());
    }

    private static List<LHPA_NameWithOfficialCode> toXmlRhy(final Collection<HarvestPermitAreaRhy> input) {
        return input.stream()
                .sorted(comparing(HarvestPermitAreaRhy::getAreaSize).reversed())
                .map(LHHarvestPermitAreaFeature::toXmlRhy)
                .collect(toList());
    }

    private static List<LHPA_Partner> toXmlPartner(final Collection<HarvestPermitAreaPartner> partners,
                                                   final Function<AreaEntity<Long>, GISZoneWithoutGeometryDTO> zoneFn) {
        return partners.stream()
                .collect(Collectors.toMap(
                        // Combine areas with same club
                        p -> p.getSourceArea().getClub(),
                        p -> toXmlPartner(p.getSourceArea().getClub(), zoneFn.apply(p)),
                        (a, b) -> {
                            final LHPA_Partner c = new LHPA_Partner();
                            c.setNameFinnish(a.getNameFinnish());
                            c.setNameSwedish(a.getNameSwedish());
                            c.setOfficialCode(a.getOfficialCode());
                            c.setLocation(a.getLocation());
                            c.setTotalAreaSize(a.getTotalAreaSize() + b.getTotalAreaSize());
                            c.setWaterAreaSize(a.getWaterAreaSize() + b.getWaterAreaSize());
                            return c;
                        }))
                .values().stream()
                .sorted(comparing(LHPA_Partner::getTotalAreaSize))
                .collect(toList());
    }

    @Nonnull
    private static LHPA_Partner toXmlPartner(final HuntingClub club, final GISZoneWithoutGeometryDTO zone) {
        final LHPA_Partner xml = new LHPA_Partner();

        xml.setOfficialCode(club.getOfficialCode());
        xml.setNameFinnish(club.getNameFinnish());
        xml.setNameSwedish(club.getNameSwedish());
        xml.setLocation(toXmlGeoLocation(club.getGeoLocation()));

        xml.setTotalAreaSize(squareMetersToHectares(zone.getComputedAreaSize()));
        xml.setWaterAreaSize(squareMetersToHectares(zone.getWaterAreaSize()));

        return xml;
    }

    private static LHPA_GeoLocation toXmlGeoLocation(final GeoLocation geoLocation) {
        if (geoLocation != null) {
            final LHPA_GeoLocation xmlLocation = new LHPA_GeoLocation();
            xmlLocation.setLatitude(geoLocation.getLatitude());
            xmlLocation.setLongitude(geoLocation.getLongitude());
            return xmlLocation;
        }
        return null;
    }

    @Nonnull
    private static LHPA_NameWithOfficialCode toXmlRhy(final HarvestPermitAreaRhy permitRhy) {
        final Riistanhoitoyhdistys rhy = permitRhy.getRhy();

        final LHPA_NameWithOfficialCode xml = new LHPA_NameWithOfficialCode();
        xml.setOfficialCode(rhy.getOfficialCode());
        xml.setNameFinnish(rhy.getNameFinnish());
        xml.setNameSwedish(rhy.getNameSwedish());
        xml.setAreaSize(squareMetersToHectares(permitRhy.getAreaSize()));

        return xml;
    }

    @Nonnull
    private static LHPA_NameWithOfficialCode toXmlHta(final HarvestPermitAreaHta permitHta) {
        final GISHirvitalousalue hta = permitHta.getHta();

        final LHPA_NameWithOfficialCode xml = new LHPA_NameWithOfficialCode();
        xml.setOfficialCode(hta.getNumber());
        xml.setNameFinnish(hta.getNameFinnish());
        xml.setNameSwedish(hta.getNameSwedish());
        xml.setAreaSize(squareMetersToHectares(permitHta.getAreaSize()));

        return xml;
    }

    private static LHPA_State toXmlState(final HarvestPermitArea.StatusCode state) {
        Objects.requireNonNull(state);

        switch (state) {
            case PENDING:
            case PROCESSING:
            case PROCESSING_FAILED:
            case INCOMPLETE:
                return LHPA_State.INCOMPLETE;
            case LOCKED:
                return LHPA_State.LOCKED;
            case READY:
                return LHPA_State.READY;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private HarvestPermitArea getByExternalIdInternal(final String externalId) {
        return harvestPermitAreaRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    return new NotFoundException("No harvest permit area found with external ID: " + externalId);
                });
    }

}
