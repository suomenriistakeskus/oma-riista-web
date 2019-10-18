package fi.riista.feature.gis.hta;

import fi.riista.util.LocalisedString;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
public class HirvitalousalueService {

    @Resource
    private GISHirvitalousalueRepository gisHirvitalousalueRepository;

    @Cacheable(value = "mooseAreaNameIndex")
    public Map<Integer, LocalisedString> getNameIndex() {
        return gisHirvitalousalueRepository.listWithoutGeometry().stream()
                .collect(toMap(HirvitalousalueDTO::getId, HirvitalousalueDTO::getNameLocalisation));
    }

}
