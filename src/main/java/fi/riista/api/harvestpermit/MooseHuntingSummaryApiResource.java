package fi.riista.api.harvestpermit;

import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryCrudFeature;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryFeature;
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

@RestController
@RequestMapping(value = "/api/v1/club/{clubId:\\d+}/moosesummary", produces = MediaType.APPLICATION_JSON_VALUE)
public class MooseHuntingSummaryApiResource {

    @Resource
    private MooseHuntingSummaryCrudFeature crudFeature;

    @Resource
    private MooseHuntingSummaryFeature feature;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MooseHuntingSummaryDTO createSummary(@RequestBody @Validated final MooseHuntingSummaryDTO dto) {
        return crudFeature.create(dto);
    }

    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MooseHuntingSummaryDTO updateSummary(@PathVariable final long id,
                                                @RequestBody @Validated final MooseHuntingSummaryDTO dto) {

        dto.setId(id);
        return crudFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/permit/{permitId:\\d+}", method = RequestMethod.GET)
    public MooseHuntingSummaryDTO findSummaryByClubIdAndPermitId(@PathVariable final long clubId,
                                                                 @PathVariable final long permitId) {

        return feature.resolveMooseHuntingSummary(clubId, permitId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/markunfinished", method = RequestMethod.POST)
    public MooseHuntingSummaryDTO markUnfinished(@SuppressWarnings("unused") @PathVariable final long clubId,
                                                 @PathVariable final long id) {

        return feature.markUnfinished(id);
    }
}
