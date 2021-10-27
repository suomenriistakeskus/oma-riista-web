package fi.riista.api.mobile;

import fi.riista.feature.organization.person.PersonSearchFeature;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/mobile/v2/search")
public class MobileSearchApiResource {

    @Resource
    private PersonSearchFeature personSearchFeature;

    @PostMapping(value = "/person/hunternumber")
    public PersonWithHunterNumberDTO findByHunterNumber(@RequestParam String hunterNumber) {
        return personSearchFeature.findNameByHunterNumber(hunterNumber);
    }
}
