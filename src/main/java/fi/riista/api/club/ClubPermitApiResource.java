package fi.riista.api.club;

import fi.riista.feature.harvestpermit.HarvestPermitDetailsFeature;
import fi.riista.feature.harvestpermit.list.HarvestPermitListFeature;
import fi.riista.feature.harvestpermit.list.MooselikeHuntingYearDTO;
import fi.riista.feature.harvestpermit.list.MooselikePermitListDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.todo.MoosePermitTodoDTO;
import fi.riista.feature.huntingclub.permit.todo.MoosePermitTodoFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/v1/club/{clubId:\\d+}/permit")
public class ClubPermitApiResource {

    @Resource
    private HarvestPermitDetailsFeature harvestPermitDetailsFeature;

    @Resource
    private HarvestPermitListFeature harvestPermitListFeature;

    @Resource
    private MoosePermitTodoFeature moosePermitTodoFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingyears", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MooselikeHuntingYearDTO> listHuntingYears(@PathVariable long clubId) {
        return harvestPermitListFeature.listClubPermitHuntingYears(clubId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping
    public List<MooselikePermitListDTO> listPermits(@PathVariable long clubId,
                                                    @RequestParam int year,
                                                    @RequestParam int species) {
        return harvestPermitListFeature.listClubPermits(clubId, year, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HuntingClubPermitDTO get(@PathVariable long clubId,
                                    @PathVariable long permitId,
                                    @RequestParam int species) {
        return harvestPermitDetailsFeature.getClubPermit(clubId, permitId, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/todo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MoosePermitTodoDTO listMoosePermitTodosForClub(@PathVariable long clubId,
                                                          @RequestParam int year) {
        return moosePermitTodoFeature.listTodosForClub(clubId, year);
    }
}
