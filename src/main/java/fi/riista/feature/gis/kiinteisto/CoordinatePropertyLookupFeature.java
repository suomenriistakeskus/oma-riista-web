package fi.riista.feature.gis.kiinteisto;

import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class CoordinatePropertyLookupFeature {
    @Resource
    private GISQueryService gisQueryService;

    @Transactional(readOnly = true)
    public Optional<MMLRekisteriyksikonTietoja> findByPosition(final GISPoint gisPoint) {
        return gisQueryService.findPropertyByLocation(gisPoint);
    }
}
