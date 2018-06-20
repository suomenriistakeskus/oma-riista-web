package fi.riista.feature.permit.area.partner;

import fi.riista.feature.gis.zone.GISZoneRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private GISZoneRepository gisZoneRepository;

    public List<HarvestPermitAreaPartnerDTO> transformAndSort(@Nonnull final List<HarvestPermitAreaPartner> list,
                                                              final Locale locale) {
        return transform(list).stream().sorted(createPartnerComparator(locale)).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    protected List<HarvestPermitAreaPartnerDTO> transform(@Nonnull final List<HarvestPermitAreaPartner> list) {
        final Function<HarvestPermitAreaPartner, HuntingClubArea> clubAreaMapping = createPartnerToAreaMapping(list);
        final Function<HuntingClubArea, HuntingClub> clubMapping = createClubMapping(F.mapNonNullsToList(list, clubAreaMapping));
        final Function<HarvestPermitAreaPartner, GISZoneWithoutGeometryDTO> zoneMapping = gisZoneRepository.getAreaMapping(list);

        return F.mapNonNullsToList(list, p -> {
            final HuntingClubArea clubArea = clubAreaMapping.apply(p);
            final GISZoneWithoutGeometryDTO zoneDTO = zoneMapping.apply(p);

            final boolean hasChanged = p.getModificationTime().before(clubArea.getModificationTime());
            final HarvestPermitAreaPartnerDTO.SourceAreaDTO sourceArea =
                    new HarvestPermitAreaPartnerDTO.SourceAreaDTO(clubArea, hasChanged);

            final OrganisationNameDTO clubDTO = OrganisationNameDTO.createWithOfficialCode(clubMapping.apply(clubArea));

            return new HarvestPermitAreaPartnerDTO(p, sourceArea, clubDTO, zoneDTO.getSize().getAll());
        });
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
