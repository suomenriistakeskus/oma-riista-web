package fi.riista.feature.moderatorarea;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.Collect.idSet;
import static java.util.stream.Collectors.toList;

@Component
public class ModeratorAreaDTOTransformer extends ListTransformer<ModeratorArea, ModeratorAreaDTO> {

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

    @Resource
    private UserRepository userRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Nonnull
    @Override
    protected List<ModeratorAreaDTO> transform(@Nonnull final List<ModeratorArea> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        final Function<ModeratorArea, SystemUser> moderatorMapping = createModeratorMapping(list);
        final Function<ModeratorArea, Organisation> rkaMapping = createRkaMapping(list);

        final Set<Long> zoneIds = list.stream().map(ModeratorArea::getZone).collect(idSet());
        final Function<ModeratorArea, GISZoneWithoutGeometryDTO> areaToZoneMapping = createAreaSizeMapping(zoneIds);
        final Function<ModeratorArea, GISBounds> areaToBoundsMapping = createAreaBoundsMapping(zoneIds);

        return list.stream().map(area -> ModeratorAreaDTO
                .create(area,
                        moderatorMapping.apply(area),
                        rkaMapping.apply(area),
                        areaToZoneMapping.apply(area),
                        areaToBoundsMapping.apply(area),
                        latestHirviYear))
                .collect(toList());
    }

    private Function<ModeratorArea, GISZoneWithoutGeometryDTO> createAreaSizeMapping(final Set<Long> zoneIds) {
        final Map<Long, GISZoneWithoutGeometryDTO> mapping = zoneRepository.fetchWithoutGeometry(zoneIds);
        return a -> a.getZone() != null ? mapping.get(F.getId(a.getZone())) : null;
    }

    private Function<ModeratorArea, GISBounds> createAreaBoundsMapping(final Set<Long> zoneIds) {
        final Map<Long, GISBounds> mapping = zoneRepository.getBounds(zoneIds, GISUtils.SRID.WGS84);
        return a -> a.getZone() != null ? mapping.get(F.getId(a.getZone())) : null;
    }

    private Function<ModeratorArea, SystemUser> createModeratorMapping(final List<ModeratorArea> list) {
        final Set<Long> userIds = list.stream().map(ModeratorArea::getModerator).collect(idSet());
        final Map<Long, SystemUser> userById = F.indexById(userRepository.findAllById(userIds));

        return a -> userById.get(F.getId(a.getModerator()));
    }

    private Function<ModeratorArea, Organisation> createRkaMapping(final List<ModeratorArea> list) {
        final Set<Long> rkaIds = list.stream().map(ModeratorArea::getRka).collect(idSet());
        final Map<Long, Organisation> rkaById = F.indexById(organisationRepository.findAllById(rkaIds));

        return a -> rkaById.get(F.getId(a.getRka()));
    }
}
