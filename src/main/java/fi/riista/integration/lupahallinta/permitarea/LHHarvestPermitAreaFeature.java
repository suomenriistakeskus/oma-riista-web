package fi.riista.integration.lupahallinta.permitarea;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.harvestpermit.area.HarvestPermitArea;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaHta;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaPartner;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaRepository;
import fi.riista.feature.harvestpermit.area.HarvestPermitAreaRhy;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Component
public class LHHarvestPermitAreaFeature {

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource(name = "lupaHallintaPermitAreaExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Transactional
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_PERMIT_AREA')")
    public void updateLockedStatus(final String externalId, final boolean isLocked) {
        final Optional<HarvestPermitArea> maybeArea = harvestPermitAreaRepository.findByExternalId(externalId);

        if (maybeArea.isPresent()) {
            final HarvestPermitArea harvestPermitArea = maybeArea.get();

            if (isLocked) {
                harvestPermitArea.setStatusLocked();
            } else {
                harvestPermitArea.setStatusReady();
            }
        } else {
            throw new NotFoundException("No such area: " + externalId);
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_PERMIT_AREA')")
    public String findByExternalId(final String externalId) {
        return harvestPermitAreaRepository.findByExternalId(externalId)
                .map(this::toXmlPermitArea)
                .map(xml -> JaxbUtils.marshalToString(xml, jaxbMarshaller))
                .orElseThrow(() -> new NotFoundException("No such area: " + externalId));
    }

    @Nonnull
    private LHPA_PermitArea toXmlPermitArea(final HarvestPermitArea area) {
        final Map<Long, GISZoneWithoutGeometryDTO> zoneSizeMapping = getZonesWithoutGeometry(area);

        final LHPA_PermitArea xml = new LHPA_PermitArea();

        xml.setNameFinnish(area.getNameFinnish());
        xml.setNameSwedish(area.getNameSwedish());
        xml.setOfficialCode(area.getExternalId());
        xml.setState(toXmlState(area.getStatus()));

        final GISZoneWithoutGeometryDTO zone = zoneSizeMapping.get(area.getZone().getId());
        xml.setTotalAreaSize(Math.round(zone.getComputedAreaSize()));
        xml.setWaterAreaSize(Math.round(zone.getWaterAreaSize()));

        xml.getRhy().addAll(toXmlRhy(area.getRhy()));
        xml.getHta().addAll(toXmlHta(area.getHta()));
        xml.getPartners().addAll(toXmlPartner(area.getPartners(), zoneSizeMapping));

        return xml;
    }

    private Map<Long, GISZoneWithoutGeometryDTO> getZonesWithoutGeometry(final HarvestPermitArea area) {
        final List<Long> zoneIds = area.getPartners().stream()
                .map(HarvestPermitAreaPartner::getZone)
                .map(HasID::getId)
                .collect(Collectors.toList());
        zoneIds.add(area.getZone().getId());

        return F.index(gisZoneRepository.fetchWithoutGeometry(zoneIds), GISZoneWithoutGeometryDTO::getId);
    }

    private static List<LHPA_NameWithOfficialCode> toXmlHta(final Collection<HarvestPermitAreaHta> input) {
        return input.stream()
                .map(LHHarvestPermitAreaFeature::toXmlHta)
                .collect(Collectors.toList());
    }

    private static List<LHPA_NameWithOfficialCode> toXmlRhy(final Collection<HarvestPermitAreaRhy> input) {
        return input.stream()
                .sorted(comparing(HarvestPermitAreaRhy::getAreaSize))
                .map(LHHarvestPermitAreaFeature::toXmlRhy)
                .collect(Collectors.toList());
    }

    private List<LHPA_Partner> toXmlPartner(final Collection<HarvestPermitAreaPartner> partners,
                                            final Map<Long, GISZoneWithoutGeometryDTO> zoneSizeMapping) {
        return partners.stream()
                .collect(Collectors.toMap(
                        // Combine areas with same club
                        p -> p.getSourceArea().getClub(),
                        p -> toXmlPartner(p, zoneSizeMapping.get(p.getZone().getId())),
                        (a, b) -> {
                            final LHPA_Partner c = new LHPA_Partner();
                            c.setNameFinnish(a.getNameFinnish());
                            c.setNameSwedish(a.getNameSwedish());
                            c.setOfficialCode(a.getOfficialCode());
                            c.setTotalAreaSize(a.getTotalAreaSize() + b.getTotalAreaSize());
                            c.setWaterAreaSize(a.getWaterAreaSize() + b.getWaterAreaSize());
                            return c;
                        }))
                .values().stream()
                .sorted(comparing(LHPA_Partner::getTotalAreaSize))
                .collect(Collectors.toList());
    }

    @Nonnull
    private LHPA_Partner toXmlPartner(final HarvestPermitAreaPartner partner,
                                      final GISZoneWithoutGeometryDTO zone) {
        final LHPA_Partner xml = new LHPA_Partner();

        final HuntingClub club = partner.getHarvestPermitArea().getClub();

        xml.setOfficialCode(club.getOfficialCode());
        xml.setNameFinnish(club.getNameFinnish());
        xml.setNameSwedish(club.getNameSwedish());

        xml.setTotalAreaSize(Math.round(zone.getComputedAreaSize()));
        xml.setWaterAreaSize(Math.round(zone.getWaterAreaSize()));

        return xml;
    }

    @Nonnull
    private static LHPA_NameWithOfficialCode toXmlRhy(final HarvestPermitAreaRhy permitRhy) {
        final Riistanhoitoyhdistys rhy = permitRhy.getRhy();

        final LHPA_NameWithOfficialCode xml = new LHPA_NameWithOfficialCode();
        xml.setOfficialCode(rhy.getOfficialCode());
        xml.setNameFinnish(rhy.getNameFinnish());
        xml.setNameSwedish(rhy.getNameSwedish());
        xml.setAreaSize(Math.round(permitRhy.getAreaSize()));

        return xml;
    }

    @Nonnull
    private static LHPA_NameWithOfficialCode toXmlHta(final HarvestPermitAreaHta permitHta) {
        final GISHirvitalousalue hta = permitHta.getHta();

        final LHPA_NameWithOfficialCode xml = new LHPA_NameWithOfficialCode();
        xml.setOfficialCode(hta.getNumber());
        xml.setNameFinnish(hta.getNameFinnish());
        xml.setNameSwedish(hta.getNameSwedish());
        xml.setAreaSize(Math.round(permitHta.getAreaSize()));

        return xml;
    }

    private static LHPA_State toXmlState(final HarvestPermitArea.StatusCode state) {
        Objects.requireNonNull(state, "state is null");

        switch (state) {
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
}
