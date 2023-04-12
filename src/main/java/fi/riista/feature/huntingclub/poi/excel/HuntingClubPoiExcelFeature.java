package fi.riista.feature.huntingclub.poi.excel;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.huntingclub.poi.PoiIdAllocationRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationGroup;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupDTO;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupTransformer;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class HuntingClubPoiExcelFeature {

    @Resource
    private PoiLocationGroupRepository repository;

    @Resource
    private PoiIdAllocationRepository idAllocationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubAreaRepository areaRepository;

    @Resource
    private PoiLocationGroupTransformer transformer;

    @Transactional(readOnly = true)
    public ClubPoiExcelDTO exportExcel(final long clubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        final OrganisationNameDTO clubDto = OrganisationNameDTO.create(club);

        final List<PoiLocationGroup> poiLocationGroups = idAllocationRepository.findByClub(club).stream()
                .findFirst()
                .map(repository::findAllByPoiIdAllocation)
                .orElse(Collections.emptyList());

        final List<PoiLocationGroupDTO> pois = transformer.apply(poiLocationGroups);

        return new ClubPoiExcelDTO(clubDto, pois);
    }

    @Transactional(readOnly = true)
    public ClubPoiExcelDTO exportExcelByArea(final long areaId) {

        final HuntingClubArea area = requireEntityService.requireHuntingClubArea(areaId, EntityPermission.READ);
        final OrganisationNameDTO clubDto = OrganisationNameDTO.create(area.getClub());

        final List<Long> poiIds = areaRepository.listPois(area.getId());
        final List<PoiLocationGroup> pois = repository.findAllById(poiIds);


        final List<PoiLocationGroupDTO> dtos = transformer.apply(pois);

        return new ClubPoiExcelDTO(clubDto, dtos);
    }
}
