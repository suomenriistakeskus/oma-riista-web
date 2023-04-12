package fi.riista.feature.training.mobile;

import fi.riista.feature.organization.jht.mobile.MobileJHTTrainingFeature;
import fi.riista.feature.organization.rhy.mobile.MobileOccupationTrainingFeature;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MobileTrainingFeature {

    @Resource
    MobileJHTTrainingFeature mobileJhtTrainingFeature;

    @Resource
    private MobileOccupationTrainingFeature mobileOccupationTrainingFeature;

    public MobileTrainingsDTO listMine() {
        return MobileTrainingsDTO.create(
            mobileJhtTrainingFeature.listMine(),
            mobileOccupationTrainingFeature.listMine()
        );
    }
}
