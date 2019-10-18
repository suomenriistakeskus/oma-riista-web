package fi.riista.feature.permit.area.partner;

import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.Locales;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.util.Collect.idSet;

@Component
public class HarvestPermitAreaPartnerDTOTransformer
        extends ListTransformer<HarvestPermitAreaPartner, HarvestPermitAreaPartnerDTO> {

    private static Comparator<HarvestPermitAreaPartnerDTO> createPartnerComparator(final Locale locale) {
        final Comparator<HarvestPermitAreaPartnerDTO> byClub =
                Comparator.comparing(p -> p.getClub().getNameLocalisation().getAnyTranslation(locale));
        final Comparator<HarvestPermitAreaPartnerDTO> byArea =
                Comparator.comparing(p -> p.getSourceArea().getName().getOrDefault(
                        locale.getLanguage(), Locales.FI_LANG));
        final Comparator<HarvestPermitAreaPartnerDTO> byExternalId =
                Comparator.comparing(p -> p.getSourceArea().getExternalId());

        return byClub.thenComparing(byArea).thenComparing(byExternalId);
    }

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private GISZoneRepository zoneRepository;

    public List<HarvestPermitAreaPartnerDTO> transformAndSort(@Nonnull final List<HarvestPermitAreaPartner> list,
                                                              final Locale locale) {
        return transform(list).stream().sorted(createPartnerComparator(locale)).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    protected List<HarvestPermitAreaPartnerDTO> transform(@Nonnull final List<HarvestPermitAreaPartner> list) {
        final Function<HarvestPermitAreaPartner, HuntingClubArea> clubAreaMapping = createPartnerToAreaMapping(list);
        final Function<HuntingClubArea, HuntingClub> clubMapping = createClubMapping(F.mapNonNullsToList(list, clubAreaMapping));
        final Function<HarvestPermitAreaPartner, GISZoneSizeDTO> areaSizeMapping = createAreaSizeMapping(list);

        return F.mapNonNullsToList(list, p -> {
            final HuntingClubArea clubArea = clubAreaMapping.apply(p);
            final GISZoneSizeDTO areaSize = areaSizeMapping.apply(p);

            final boolean hasChanged = p.getModificationTime().before(clubArea.getModificationTime());
            final HarvestPermitAreaPartnerDTO.SourceAreaDTO sourceArea =
                    new HarvestPermitAreaPartnerDTO.SourceAreaDTO(clubArea, hasChanged);

            final OrganisationNameDTO clubDTO = OrganisationNameDTO.createWithOfficialCode(clubMapping.apply(clubArea));

            return new HarvestPermitAreaPartnerDTO(p, sourceArea, clubDTO, areaSize);
        });
    }

    private Function<HarvestPermitAreaPartner, GISZoneSizeDTO> createAreaSizeMapping(
            final List<HarvestPermitAreaPartner> list) {
        final Set<Long> zoneIds = list.stream().map(HarvestPermitAreaPartner::getZone).collect(idSet());
        final Map<Long, GISZoneWithoutGeometryDTO> mapping = zoneRepository.fetchWithoutGeometry(zoneIds);

        return a -> Optional.of(a)
                .map(HarvestPermitAreaPartner::getZone)
                .map(F::getId)
                .map(mapping::get)
                .map(GISZoneWithoutGeometryDTO::getSize)
                .orElse(null);
    }

    private Function<HarvestPermitAreaPartner, HuntingClubArea> createPartnerToAreaMapping(
            final Iterable<HarvestPermitAreaPartner> groups) {

        return CriteriaUtils.singleQueryFunction(
                groups, HarvestPermitAreaPartner::getSourceArea, huntingClubAreaRepository, true);
    }

    private Function<HuntingClubArea, HuntingClub> createClubMapping(final Iterable<HuntingClubArea> areas) {
        return CriteriaUtils.singleQueryFunction(areas, HuntingClubArea::getClub, huntingClubRepository, true);
    }
}
