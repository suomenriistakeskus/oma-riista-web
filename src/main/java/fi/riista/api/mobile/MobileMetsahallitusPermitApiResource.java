package fi.riista.api.mobile;

import fi.riista.integration.metsahallitus.mobile.MetsahallitusMobilePermitDTO;
import fi.riista.integration.metsahallitus.mobile.MetsahallitusMobilePermitListFeature;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/mobile/v2/permit/mh")
public class MobileMetsahallitusPermitApiResource {

    @Resource
    private MetsahallitusMobilePermitListFeature metsahallitusMobilePermitListFeature;

    @GetMapping
    public List<MetsahallitusMobilePermitDTO> listAll() {
        return metsahallitusMobilePermitListFeature.listAll();
    }
}
