package fi.riista.api;

import fi.riista.feature.common.ContentTypeChecker;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingHarvestReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingHarvestReportFeature;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingHarvestReportStateChangeDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportFeature;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/harvestreport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EndOfHuntingReportApiResource {

    @Resource
    private EndOfHuntingHarvestReportFeature endOfHuntingHarvestReportFeature;

    @Resource
    private MooseHarvestReportFeature mooseHarvestReportCrudFeature;

    @Resource
    private ContentTypeChecker contentTypeChecker;

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

    @PostMapping("/permit/{permitId:\\d+}")
    public EndOfHuntingHarvestReportDTO createEndOfHuntingReport(final @PathVariable long permitId) {
        return endOfHuntingHarvestReportFeature.createEndOfHuntingReport(permitId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/permit/{permitId:\\d+}")
    public void deleteEndOfHuntingReport(final @PathVariable long permitId) {
        endOfHuntingHarvestReportFeature.deleteEndOfHuntingReport(permitId);
    }

    // Moose permit

    @PostMapping("/moosepermit/{permitId:\\d+}/{speciesCode:\\d+}")
    public ResponseEntity<?> createMooseHarvestReport(final @PathVariable long permitId,
                                                      final @PathVariable int speciesCode,
                                                      final @RequestParam("file") MultipartFile file) {
        if (!contentTypeChecker.isValidAttachmentContent(file)) {
            return ResponseEntity.badRequest().build();
        }

        mooseHarvestReportCrudFeature.create(MooseHarvestReportDTO.withReceipt(permitId, speciesCode, file));

        return ResponseEntity.ok().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/moosepermit/{permitId:\\d+}/{speciesCode:\\d+}/noharvests")
    public void createMooseHarvestReportNoHarvests(final @PathVariable long permitId,
                                                   final @PathVariable int speciesCode) {
        mooseHarvestReportCrudFeature.create(MooseHarvestReportDTO.withNoHarvests(permitId, speciesCode));
    }

    @DeleteMapping("/moosepermit/{permitId:\\d+}/{speciesCode:\\d+}")
    public void deleteMooseHarvestReport(final @PathVariable long permitId,
                                         final @PathVariable int speciesCode) {
        mooseHarvestReportCrudFeature.delete(permitId, speciesCode);
    }

    @PostMapping("/moosepermit/{permitId:\\d+}/{speciesCode:\\d+}/receipt")
    public ResponseEntity<byte[]> getReceiptFile(final @PathVariable long permitId,
                                                 final @PathVariable int speciesCode) throws IOException {
        return mooseHarvestReportCrudFeature.getReceiptFile(permitId, speciesCode);
    }

}
