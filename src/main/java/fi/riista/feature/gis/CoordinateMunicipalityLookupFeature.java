package fi.riista.feature.gis;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class CoordinateMunicipalityLookupFeature {

    private static final Logger LOG = LoggerFactory.getLogger(CoordinateMunicipalityLookupFeature.class);

    @Resource
    private GISQueryService gisQueryService;

    @Transactional(readOnly = true)
    public OrganisationNameDTO findByGeoLocation(final GeoLocation geoLocation) {
        final Municipality municipality = gisQueryService.findMunicipality(geoLocation);

        if (municipality == null) {
            LOG.warn("Municipality not found for coordinates {}, {}", geoLocation.getLatitude(), geoLocation.getLongitude());
            return null;
        }

        LOG.debug("Found GIS municipality: {}", municipality.getOfficialCode());

        final OrganisationNameDTO dto = new OrganisationNameDTO();
        final LocalisedString nameLocalisation = municipality.getNameLocalisation();
        dto.setNameFI(nameLocalisation.getFinnish());
        dto.setNameSV(nameLocalisation.getSwedish());

        return dto;
    }
}
