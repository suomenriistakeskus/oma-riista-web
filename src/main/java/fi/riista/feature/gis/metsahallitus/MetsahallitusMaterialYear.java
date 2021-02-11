package fi.riista.feature.gis.metsahallitus;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.util.DateUtil.currentYear;

@Component
public class MetsahallitusMaterialYear {

    @Resource
    private MetsahallitusHirviRepository metsahallitusHirviRepository;

    @Resource
    private MetsahallitusPienriistaRepository metsahallitusPienriistaRepository;

    @Cacheable("latestMetsahallitusHirviYear")
    @Transactional(readOnly = true)
    public int getLatestHirviYear() {
        return Optional.ofNullable(metsahallitusHirviRepository.findLatestYear()).orElse(currentYear());
    }

    @Cacheable("latestMetsahallitusPienriistaYear")
    @Transactional(readOnly = true)
    public int getLatestPienriistaYear() {
        return Optional.ofNullable(metsahallitusPienriistaRepository.findLatestYear()).orElse(currentYear());
    }
}
