package fi.riista.api.harvestpermit;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.DeerHuntingSummaryFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/deersummary", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeerHuntingSummaryApiResource {

    @Resource
    private DeerHuntingSummaryFeature deerHuntingSummaryFeature;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public BasicClubHuntingSummaryDTO createSummary(@RequestBody @Validated final BasicClubHuntingSummaryDTO dto) {
        return deerHuntingSummaryFeature.create(dto);
    }

    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public BasicClubHuntingSummaryDTO updateSummary(
            @PathVariable final long id, @RequestBody @Validated final BasicClubHuntingSummaryDTO dto) {

        dto.setId(id);
        return deerHuntingSummaryFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/club/{clubId:\\d+}/speciesamount/{speciesAmountId:\\d+}", method = RequestMethod.GET)
    public BasicClubHuntingSummaryDTO findSummaryByClubIdAndSpeciesAmountId(
            @PathVariable final long clubId, @PathVariable final long speciesAmountId) {

        return deerHuntingSummaryFeature.getDeerSummary(clubId, speciesAmountId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(
            value = "/editstate/club/{clubId:\\d+}/speciesamount/{speciesAmountId:\\d+}", method = RequestMethod.GET)
    public Map<String, Object> getEditState(
            @PathVariable final long clubId, @PathVariable final long speciesAmountId) {

        return ImmutableMap.of("isLocked", deerHuntingSummaryFeature.isLocked(clubId, speciesAmountId));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/markunfinished", method = RequestMethod.POST)
    public BasicClubHuntingSummaryDTO markUnfinished(@PathVariable final long id) {
        return deerHuntingSummaryFeature.markUnfinished(id);
    }

}
