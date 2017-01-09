package fi.riista.feature.gis.metsahallitus;

import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
@Transactional
public class MetsahallitusGeometryLookupFeature {
    @Resource
    private GISMetsahallitusRepository metsahallitusHirviRepository;

    @Transactional(readOnly = true)
    public List<GISMetsahallitusHirviDTO> listHirvi(final int year) {
        return metsahallitusHirviRepository.listHirvi(year);
    }

    @Transactional(readOnly = true)
    public Feature getHirviFeature(final int id) {
        return metsahallitusHirviRepository.getHirviFeature(id, GISUtils.SRID.WGS84);
    }
}
