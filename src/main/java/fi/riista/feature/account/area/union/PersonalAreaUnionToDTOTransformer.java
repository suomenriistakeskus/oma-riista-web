package fi.riista.feature.account.area.union;

import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerService;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.Collect.idSet;

@Component
public class PersonalAreaUnionToDTOTransformer extends ListTransformer<PersonalAreaUnion, PersonalAreaUnionDTO> {

    @Resource
    private HarvestPermitAreaPartnerService harvestPermitAreaPartnerService;
    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private LastModifierService lastModifierService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Nonnull
    private Function<PersonalAreaUnion, HarvestPermitArea> getAreaUnionToPermitAreaMapping(final Iterable<PersonalAreaUnion> items) {
        return CriteriaUtils.singleQueryFunction(items, PersonalAreaUnion::getHarvestPermitArea,
                harvestPermitAreaRepository, true);
    }

    @Nonnull
    private Function<HarvestPermitArea, GISZone> getPermitAreaToZoneMapping(final Iterable<HarvestPermitArea> items) {
        return CriteriaUtils.singleQueryFunction(items, HarvestPermitArea::getZone,
                gisZoneRepository, true);
    }

    public PersonalAreaUnionDTO transform(@Nonnull final PersonalAreaUnion areaUnion) {
        final Locale locale = LocaleContextHolder.getLocale();
        final HarvestPermitArea harvestPermitArea = areaUnion.getHarvestPermitArea();
        final Long zoneId = harvestPermitArea.getZone().getId();
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(zoneId);
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.WGS84);

        return doTransform(
                areaUnion,
                harvestPermitArea,
                areaSize,
                bounds,
                lastModifierService.getLastModifier(areaUnion),
                locale);
    }

    private PersonalAreaUnionDTO doTransform(final PersonalAreaUnion areaUnion,
                                             final HarvestPermitArea harvestPermitArea,
                                             final GISZoneSizeDTO sizeDTO,
                                             final GISBounds bounds,
                                             final LastModifierDTO lastModifierDTO,
                                             final Locale locale) {

        return new PersonalAreaUnionDTO(areaUnion, lastModifierDTO, harvestPermitArea,
                sizeDTO, bounds,
                harvestPermitAreaPartnerService.listPartners(harvestPermitArea, locale)); // TODO: N+1
    }

    @Nonnull
    @Override
    protected List<PersonalAreaUnionDTO> transform(@Nonnull final List<PersonalAreaUnion> list) {
        final Locale locale = LocaleContextHolder.getLocale();
        final Function<PersonalAreaUnion, HarvestPermitArea> areaUnionToPermitAreaMapping =
                getAreaUnionToPermitAreaMapping(list);

        final Map<PersonalAreaUnion, LastModifierDTO> lastModifiers = lastModifierService.getLastModifiers(list);
        final Set<Long> zoneIds =
                F.stream(list).map(PersonalAreaUnion::getHarvestPermitArea).map(HarvestPermitArea::getZone).collect(idSet());
        final Function<PersonalAreaUnion, GISZoneWithoutGeometryDTO> areaToZoneMapping = createAreaSizeMapping(zoneIds);
        final Function<PersonalAreaUnion, GISBounds> areaToBoundsMapping = createAreaBoundsMapping(zoneIds);

        return F.mapNonNullsToList(list, accountArea -> {
            final HarvestPermitArea harvestPermitArea = areaUnionToPermitAreaMapping.apply(accountArea);
            final GISZoneWithoutGeometryDTO gisZoneWithoutGeometryDTO = areaToZoneMapping.apply(accountArea);
            final GISBounds gisBounds = areaToBoundsMapping.apply(accountArea);
            return doTransform(
                    accountArea,
                    harvestPermitArea,
                    gisZoneWithoutGeometryDTO.getSize(),
                    gisBounds,
                    lastModifiers.get(accountArea),
                    locale);
        });

    }

    private Function<PersonalAreaUnion, GISZoneWithoutGeometryDTO> createAreaSizeMapping(final Set<Long> zoneIds) {
        final Map<Long, GISZoneWithoutGeometryDTO> mapping = gisZoneRepository.fetchWithoutGeometry(zoneIds);
        return a -> a.getHarvestPermitArea().getZone() != null ?
                mapping.get(F.getId(a.getHarvestPermitArea().getZone())) : null;
    }

    private Function<PersonalAreaUnion, GISBounds> createAreaBoundsMapping(final Set<Long> zoneIds) {
        final Map<Long, GISBounds> mapping = gisZoneRepository.getBounds(zoneIds, GISUtils.SRID.WGS84);
        return a -> a.getHarvestPermitArea().getZone() != null ?
                mapping.get(F.getId(a.getHarvestPermitArea().getZone())) : null;
    }
}
