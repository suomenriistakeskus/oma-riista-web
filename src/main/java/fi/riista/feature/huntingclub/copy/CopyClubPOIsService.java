package fi.riista.feature.huntingclub.copy;

import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class CopyClubPOIsService {

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void copyPOIsHavingArea(final HuntingClubArea originalArea, final HuntingClubArea area) {
        huntingClubAreaRepository.addPois(area.getId(),
                huntingClubAreaRepository.listPois(originalArea.getId()));
    }
}
