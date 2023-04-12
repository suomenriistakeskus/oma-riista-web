package fi.riista.feature.huntingclub.poi.excel;

import fi.riista.feature.huntingclub.poi.PoiLocationGroupDTO;
import fi.riista.feature.organization.OrganisationNameDTO;

import java.util.List;

public class ClubPoiExcelDTO {

    private final OrganisationNameDTO club;


    private final List<PoiLocationGroupDTO> pois;

    public ClubPoiExcelDTO(final OrganisationNameDTO club,
                           final List<PoiLocationGroupDTO> pois) {
        this.club = club;
        this.pois = pois;
    }

    public OrganisationNameDTO getClub() {
        return club;
    }


    public List<PoiLocationGroupDTO> getPois() {
        return pois;
    }
}
