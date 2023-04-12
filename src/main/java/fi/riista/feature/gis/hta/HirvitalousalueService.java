package fi.riista.feature.gis.hta;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.LocalisedString;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class HirvitalousalueService {

    @Resource
    private GISHirvitalousalueRepository gisHirvitalousalueRepository;

    @Cacheable(value = "mooseAreaNameIndex")
    public Map<Integer, LocalisedString> getNameIndex() {
        return gisHirvitalousalueRepository.listWithoutGeometry().stream()
                .collect(toMap(HirvitalousalueDTO::getId, HirvitalousalueDTO::getNameLocalisation));
    }

    @Cacheable(value = "mooseAreaIndex")
    public GISHirvitalousalue getOne(final int htaId) {
        final Specification<GISHirvitalousalue> spec =
                where(equal(GISHirvitalousalue_.id, htaId));
        return gisHirvitalousalueRepository.findOne(spec).orElse(null);
    }

    @Cacheable(value = "mooseAreaRHYNameIndex")
    public Map<Integer, LocalisedString> findByRHY(final Riistanhoitoyhdistys rhy) {
        return gisHirvitalousalueRepository.findByRHY(rhy).stream()
                .collect(toMap(HirvitalousalueDTO::getId, HirvitalousalueDTO::getNameLocalisation));
    }

}
