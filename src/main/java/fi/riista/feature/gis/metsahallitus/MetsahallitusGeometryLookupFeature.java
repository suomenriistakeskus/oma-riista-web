package fi.riista.feature.gis.metsahallitus;

import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class MetsahallitusGeometryLookupFeature {

    @Resource
    private MetsahallitusHirviRepository metsahallitusHirviRepository;

    @Resource
    private MetsahallitusPienriistaRepository metsahallitusPienriistaRepository;

    @Transactional(readOnly = true)
    public List<MetsahallitusHirviDTO> listHirviAll(final int year) {
        return metsahallitusHirviRepository.findAll(year);
    }

    @Transactional(readOnly = true)
    public List<MetsahallitusHirviDTO> listHirviPublic(final int year) {
        return metsahallitusHirviRepository.findAllPublic(year);
    }

    @Transactional(readOnly = true)
    public List<MetsahallitusPienriistaDTO> listPienriista(final int year) {
        return metsahallitusPienriistaRepository.findAll(year);
    }

    @Transactional(readOnly = true)
    public Feature getHirviFeature(final int id) {
        return metsahallitusHirviRepository.findFeature(id, GISUtils.SRID.WGS84);
    }
}
