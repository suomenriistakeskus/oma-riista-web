package fi.riista.api;

import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitFeature;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.huntingclub.permit.MooselikePermitListingDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/club/{clubId:\\d+}/permit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClubPermitApiResource {

    @Resource
    private HuntingClubPermitFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public List<MooselikePermitListingDTO> listPermits(@PathVariable long clubId,
                                                       @RequestParam int year,
                                                       @RequestParam int species) {
        return feature.listPermits(clubId, year, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "species", method = RequestMethod.GET)
    public List<GameSpeciesDTO> listPermitSpecies(@PathVariable long clubId) {
        return feature.listPermitSpecies(clubId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{permitId:\\d+}", method = RequestMethod.GET)
    public HuntingClubPermitDTO get(@PathVariable long clubId,
                                    @PathVariable long permitId,
                                    @RequestParam int species) {
        return feature.getPermit(clubId, permitId, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/huntingyears", method = RequestMethod.GET)
    public List<MooselikeHuntingYearDTO> listHuntingYears(@PathVariable long clubId) {
        return feature.listHuntingYears(clubId);
    }
}
