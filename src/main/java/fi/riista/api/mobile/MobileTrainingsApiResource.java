package fi.riista.api.mobile;

import fi.riista.feature.training.mobile.MobileTrainingFeature;
import fi.riista.feature.training.mobile.MobileTrainingsDTO;
import javax.annotation.Resource;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/mobile/v2/trainings")
public class MobileTrainingsApiResource {

    @Resource
    private MobileTrainingFeature mobileTrainingFeature;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public MobileTrainingsDTO myTrainings() {
        return mobileTrainingFeature.listMine();
    }
}
