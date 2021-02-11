package fi.riista.feature.moderatorarea;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class ModeratorAreaCopyFeature {

    @Resource
    private ModeratorAreaRepository moderatorAreaRepository;

    @Resource
    private ModeratorAreaDTOTransformer dtoTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public ModeratorAreaDTO copy(final long areaId, final int year) {
        final ModeratorArea originalArea = requireEntityService.requireModeratorArea(areaId, EntityPermission.CREATE);

        final ModeratorArea area = new ModeratorArea();
        area.setModerator(activeUserService.requireActiveUser());
        area.setName(originalArea.getName());
        area.setYear(year);
        area.setRka(originalArea.getRka());

        area.generateAndStoreExternalId(secureRandom);

        Optional.ofNullable(originalArea.getZone())
                .map(originalZone -> gisZoneRepository.copyZone(originalZone, new GISZone()))
                .ifPresent(area::setZone);

        moderatorAreaRepository.saveAndFlush(area);

        return dtoTransformer.apply(area);
    }
}
