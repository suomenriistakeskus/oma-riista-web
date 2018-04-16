package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.gis.zone.AbstractAreaDTOTransformer;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitAreaPartnerDTOTransformer
        extends AbstractAreaDTOTransformer<HarvestPermitAreaPartner, HarvestPermitAreaPartnerDTO> {

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Nonnull
    @Override
    protected List<HarvestPermitAreaPartnerDTO> transform(@Nonnull final List<HarvestPermitAreaPartner> list) {
        final Function<HarvestPermitAreaPartner, HuntingClubArea> clubAreaMapping = createPartnerToAreaMapping(list);
        final List<HuntingClubArea> clubAreas = list.stream().map(clubAreaMapping).collect(toList());
        final Function<HuntingClubArea, HuntingClub> clubMapping = createClubMapping(clubAreas);
        final Function<HarvestPermitAreaPartner, GISZoneWithoutGeometryDTO> zoneMapping = createZoneDTOFunction(list);
        final Function<HarvestPermitAreaPartner, GISZoneWithoutGeometryDTO> originalZoneMapping =
                createOriginalZoneMapping(clubAreas);

        return list.stream()
                .map(p -> {
                    final HuntingClubArea clubArea = clubAreaMapping.apply(p);
                    final HuntingClub club = clubMapping.apply(clubArea);
                    final GISZoneWithoutGeometryDTO zoneDTO = zoneMapping.apply(p);
                    final GISZoneWithoutGeometryDTO originalZoneDTO = originalZoneMapping.apply(p);
                    final Optional<Date> originalZoneMtime = Optional.ofNullable(originalZoneDTO)
                            .map(GISZoneWithoutGeometryDTO::getModificationTime);

                    return new HarvestPermitAreaPartnerDTO(p, clubArea, club, zoneDTO, originalZoneMtime);
                })
                .collect(toList());
    }

    private Function<HarvestPermitAreaPartner, HuntingClubArea> createPartnerToAreaMapping(
            final Iterable<HarvestPermitAreaPartner> groups) {

        return CriteriaUtils.singleQueryFunction(
                groups, HarvestPermitAreaPartner::getSourceArea, huntingClubAreaRepository, true);
    }

    private Function<HuntingClubArea, HuntingClub> createClubMapping(final Iterable<HuntingClubArea> areas) {
        return CriteriaUtils.singleQueryFunction(areas, HuntingClubArea::getClub, huntingClubRepository, true);
    }

    private Function<HarvestPermitAreaPartner, GISZoneWithoutGeometryDTO> createOriginalZoneMapping(
            final Iterable<HuntingClubArea> sourceAreas) {

        return createZoneDTOFunction(sourceAreas).compose(HarvestPermitAreaPartner::getSourceArea);
    }

}
