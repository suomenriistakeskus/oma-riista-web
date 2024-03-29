package fi.riista.api.harvestpermit;

import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingHarvestReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingHarvestReportFeature;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingHarvestReportStateChangeDTO;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingNestRemovalReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingNestRemovalReportFeature;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingReportModeratorCommentsDTO;
import fi.riista.feature.harvestpermit.endofhunting.EndOfMooselikePermitHuntingFeature;
import fi.riista.feature.harvestpermit.endofhunting.EndOfPermitPeriodReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.EndOfPermitPeriodReportFeature;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/harvestreport", produces = MediaType.APPLICATION_JSON_VALUE)
public class EndOfHuntingReportApiResource {

    @Resource
    private EndOfHuntingHarvestReportFeature endOfHuntingHarvestReportFeature;

    @Resource
    private EndOfMooselikePermitHuntingFeature endOfMooselikePermitHuntingFeature;

    @Resource
    private EndOfHuntingNestRemovalReportFeature endOfHuntingNestRemovalReportFeature;

    @Resource
    private EndOfPermitPeriodReportFeature endOfPermitPeriodReportFeature;

    // Normal permit

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/permit/{permitId:\\d+}/state")
    public void changeEndOfHuntingReportState(final @PathVariable long permitId,
                                              final @RequestBody @Valid EndOfHuntingHarvestReportStateChangeDTO dto) {
        dto.setId(permitId);
        endOfHuntingHarvestReportFeature.changeEndOfHuntingHarvestReportState(dto);
    }

    @GetMapping("/permit/{permitId:\\d+}")
    public EndOfHuntingHarvestReportDTO getEndOfHuntingReport(final @PathVariable long permitId) {
        return endOfHuntingHarvestReportFeature.getEndOfHuntingReport(permitId);
    }

    @GetMapping("/permit/nestremoval/{permitId:\\d+}")
    public EndOfHuntingNestRemovalReportDTO getNestRemovalEndOfHuntingReport(final @PathVariable long permitId) {
        return endOfHuntingNestRemovalReportFeature.getEndOfNestRemovalPermitReport(permitId);
    }

    @GetMapping("/endofpermitperiod/permit/{permitId:\\d+}")
    public EndOfPermitPeriodReportDTO getEndOfPermitPeriodReport(final @PathVariable long permitId) {
        return endOfPermitPeriodReportFeature.getEndOfPermitPeriodReport(permitId);
    }

    @PostMapping("/permit/{permitId:\\d+}")
    public EndOfHuntingHarvestReportDTO createEndOfHuntingReport(final @PathVariable long permitId) {
        return endOfHuntingHarvestReportFeature.createEndOfHuntingReport(permitId);
    }

    @PostMapping("/permit/{permitId:\\d+}/moderator")
    public EndOfHuntingHarvestReportDTO moderatorCreateEndOfHuntingReport(final @PathVariable long permitId,
                                                                          final @RequestBody @Valid EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        return endOfHuntingHarvestReportFeature.createEndOfHuntingReport(permitId, endOfHuntingReportComments);
    }

    @PostMapping("/permit/nestremoval/{permitId:\\d+}")
    public EndOfHuntingNestRemovalReportDTO createNestRemovalEndOfHuntingReport(final @PathVariable long permitId) {
        return endOfHuntingNestRemovalReportFeature.createEndOfHuntingReport(permitId, null);
    }

    @PostMapping("/permit/nestremoval/{permitId:\\d+}/moderator")
    public EndOfHuntingNestRemovalReportDTO moderatorCreateNestRemovalEndOfHuntingReport(final @PathVariable long permitId,
                                                                                         final @RequestBody @Valid EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        return endOfHuntingNestRemovalReportFeature.createEndOfHuntingReport(permitId, endOfHuntingReportComments);
    }

    @PostMapping("/endofpermitperiod/permit/{permitId:\\d+}")
    public EndOfPermitPeriodReportDTO createEndOfPermitPeriodReport(final @PathVariable long permitId) {
        return endOfPermitPeriodReportFeature.createEndOfPermitPeriodReport(permitId, null);
    }

    @PostMapping("/endofpermitperiod/permit/{permitId:\\d+}/moderator")
    public EndOfPermitPeriodReportDTO moderatorCreateEndOfPermitPeriodReport(final @PathVariable long permitId,
                                                                             final @RequestBody @Valid EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        return endOfPermitPeriodReportFeature.createEndOfPermitPeriodReport(permitId, endOfHuntingReportComments);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/permit/{permitId:\\d+}")
    public void deleteEndOfHuntingReport(final @PathVariable long permitId) {
        endOfHuntingHarvestReportFeature.deleteEndOfHuntingReport(permitId);
    }

    // Moose permit

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/moosepermit/{permitId:\\d+}/{speciesCode:\\d+}")
    public void endMooselikeHunting(final @PathVariable long permitId,
                                    final @PathVariable int speciesCode) throws IOException {
        endOfMooselikePermitHuntingFeature.endMooselikeHunting(permitId, speciesCode);
    }

    @DeleteMapping("/moosepermit/{permitId:\\d+}/{speciesCode:\\d+}")
    public void cancelEndMooselikeHunting(final @PathVariable long permitId,
                                          final @PathVariable int speciesCode) {
        endOfMooselikePermitHuntingFeature.cancelEndMooselikeHunting(permitId, speciesCode);
    }

}
