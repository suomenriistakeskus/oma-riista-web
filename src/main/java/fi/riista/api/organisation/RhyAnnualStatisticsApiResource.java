package fi.riista.api.organisation;

import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.organization.rhy.annualstats.AnnualShootingTestStatistics;
import fi.riista.feature.organization.rhy.annualstats.AnnualShootingTestStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.CommunicationStatistics;
import fi.riista.feature.organization.rhy.annualstats.GameDamageStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.HunterExamTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamTrainingStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.HunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.YouthTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HuntingControlStatistics;
import fi.riista.feature.organization.rhy.annualstats.JHTTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.MetsahallitusStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHuntingRelatedStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherPublicAdminStatistics;
import fi.riista.feature.organization.rhy.annualstats.PublicEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsCrudFeature;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfoDTO;
import fi.riista.feature.organization.rhy.annualstats.ShootingRangeStatistics;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportFeature;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsProgressDTO;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsWorkflowFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;
import static fi.riista.util.MediaTypeExtras.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys/annualstatistics", produces = APPLICATION_JSON_UTF8_VALUE)
public class RhyAnnualStatisticsApiResource {

    @Resource
    private RhyAnnualStatisticsCrudFeature annualStatisticsCrudFeature;

    @Resource
    private RhyAnnualStatisticsWorkflowFeature annualStatisticsWorkflowFeature;

    @Resource
    private AnnualStatisticsExportFeature annualStatisticsExportFeature;

    @PutMapping(value = "/{annualStatisticsId:\\d+}/basicinfo", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateBasicInfo(final @PathVariable long annualStatisticsId,
                                                  final @RequestBody @Valid RhyBasicInfoDTO dto) {

        return annualStatisticsCrudFeature.moderatorUpdateBasicInfo(annualStatisticsId, dto);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/hunterexams", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateHunterExams(final @PathVariable long annualStatisticsId,
                                                    final @RequestBody @Valid HunterExamStatistics input) {

        return annualStatisticsCrudFeature.updateHunterExams(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/moderatedhunterexams", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO moderatorUpdateHunterExams(final @PathVariable long annualStatisticsId,
                                                             final @RequestBody @Valid HunterExamStatisticsDTO dto) {

        return annualStatisticsCrudFeature.moderatorUpdateHunterExams(annualStatisticsId, dto);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/moderatedshootingtests", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO moderatorUpdateShootingTests(final @PathVariable long annualStatisticsId,
                                                               final @RequestBody @Valid AnnualShootingTestStatisticsDTO dto) {

        return annualStatisticsCrudFeature.moderatorUpdateShootingTests(annualStatisticsId, dto);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/huntingcontrol", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateHuntingControl(final @PathVariable long annualStatisticsId,
                                                       final @RequestBody @Valid HuntingControlStatistics input) {

        return annualStatisticsCrudFeature.updateHuntingControl(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/gamedamage", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateGameDamage(final @PathVariable long annualStatisticsId,
                                                   final @RequestBody @Valid GameDamageStatistics input) {

        return annualStatisticsCrudFeature.updateGameDamage(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/otherpublicadmin", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateOtherPublicAdmin(final @PathVariable long annualStatisticsId,
                                                         final @RequestBody @Valid OtherPublicAdminStatistics input) {

        return annualStatisticsCrudFeature.updateOtherPublicAdmin(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/hunterexamtraining", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateHunterExamTraining(final @PathVariable long annualStatisticsId,
                                                           final @RequestBody @Valid HunterExamTrainingStatistics input) {

        return annualStatisticsCrudFeature.updateHunterExamTraining(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/moderatedhunterexamtraining", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO moderatorUpdateHunterExamTraining(final @PathVariable long annualStatisticsId,
                                                                    final @RequestBody @Valid HunterExamTrainingStatisticsDTO dto) {

        return annualStatisticsCrudFeature.moderatorUpdateHunterExamTraining(annualStatisticsId, dto);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/jhttraining", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateJhtTraining(final @PathVariable long annualStatisticsId,
                                                    final @RequestBody @Valid JHTTrainingStatistics input) {

        return annualStatisticsCrudFeature.updateJhtTraining(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/huntertraining", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateHunterTraining(final @PathVariable long annualStatisticsId,
                                                       final @RequestBody @Valid HunterTrainingStatistics input) {

        return annualStatisticsCrudFeature.updateHunterTraining(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/youthtraining", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateYouthTraining(final @PathVariable long annualStatisticsId,
                                                       final @RequestBody @Valid YouthTrainingStatistics input) {

        return annualStatisticsCrudFeature.updateYouthTraining(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/otherhuntertraining", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateOtherHunterTraining(final @PathVariable long annualStatisticsId,
                                                            final @RequestBody @Valid OtherHunterTrainingStatistics input) {

        return annualStatisticsCrudFeature.updateOtherHunterTraining(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/publicevents", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updatePublicEvents(final @PathVariable long annualStatisticsId,
                                                     final @RequestBody @Valid PublicEventStatistics input) {

        return annualStatisticsCrudFeature.updatePublicEvents(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/otherhuntingrelated", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateOtherHuntingRelated(final @PathVariable long annualStatisticsId,
                                                            final @RequestBody @Valid OtherHuntingRelatedStatistics input) {

        return annualStatisticsCrudFeature.updateOtherHuntingRelated(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/moderatedotherhuntingrelated", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO moderatorUpdateOtherHuntingRelated(final @PathVariable long annualStatisticsId,
                                                                     final @RequestBody @Valid OtherHuntingRelatedStatistics input) {

        return annualStatisticsCrudFeature.moderatorUpdateOtherHuntingRelated(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/communication", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateCommunication(final @PathVariable long annualStatisticsId,
                                                      final @RequestBody @Valid CommunicationStatistics input) {

        return annualStatisticsCrudFeature.updateCommunication(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/shootingranges", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateShootingRanges(final @PathVariable long annualStatisticsId,
                                                       final @RequestBody @Valid ShootingRangeStatistics input) {

        return annualStatisticsCrudFeature.updateShootingRanges(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/luke", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateLuke(final @PathVariable long annualStatisticsId,
                                             final @RequestBody @Valid LukeStatistics input) {

        return annualStatisticsCrudFeature.moderatorUpdateLuke(annualStatisticsId, input);
    }

    @PutMapping(value = "/{annualStatisticsId:\\d+}/metsahallitus", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateMetsahallitus(final @PathVariable long annualStatisticsId,
                                                      final @RequestBody @Valid MetsahallitusStatistics input) {

        return annualStatisticsCrudFeature.moderatorUpdateMetsahallitus(annualStatisticsId, input);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/year/{year:\\d+}")
    public List<RhyAnnualStatisticsProgressDTO> listAnnualStatistics(final @PathVariable int year) {
        return annualStatisticsWorkflowFeature.listAnnualStatistics(year);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{annualStatisticsId:\\d+}/submitforinspection", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void submitForInspection(final @PathVariable long annualStatisticsId,
                                    final @RequestBody @Valid IdRevisionDTO dto) {

        dto.setId(annualStatisticsId);
        annualStatisticsWorkflowFeature.submitForInspection(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{annualStatisticsId:\\d+}/approve", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void approve(final @PathVariable long annualStatisticsId, final @RequestBody @Valid IdRevisionDTO dto) {
        dto.setId(annualStatisticsId);
        annualStatisticsWorkflowFeature.approve(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/year/{year:\\d+}/approve", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void approve(final @PathVariable int year, final @RequestBody List<Long> annualStatisticsIds) {
        annualStatisticsWorkflowFeature.batchApprove(year, annualStatisticsIds);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{annualStatisticsId:\\d+}/cancelapproval", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void cancelApproval(final @PathVariable long annualStatisticsId,
                               final @RequestBody @Valid IdRevisionDTO dto) {

        dto.setId(annualStatisticsId);
        annualStatisticsWorkflowFeature.cancelApproval(dto);
    }

    @PostMapping(value = "/{annualStatisticsId:\\d+}/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportStatisticsToExcel(final @PathVariable long annualStatisticsId, final Locale locale) {
        return new ModelAndView(annualStatisticsExportFeature.exportAnnualStatisticsToExcel(annualStatisticsId, locale));
    }

    @PostMapping(value = "/{annualStatisticsId:\\d+}/pdf",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportStatisticsToPdf(final @PathVariable long annualStatisticsId,
                                                        final Locale locale) {

        return annualStatisticsExportFeature.exportAnnualStatisticsToPdf(annualStatisticsId, locale);
    }

    @PostMapping(value = "/year/{year:\\d+}/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportAllStatisticsToExcel(final @PathVariable int year,
                                                   final @RequestParam boolean groupByRka,
                                                   final Locale locale) {

        return new ModelAndView(annualStatisticsExportFeature.exportAllAnnualStatistics(year, locale, groupByRka));
    }

    @PostMapping(value = "/year/{year:\\d+}/sendersexcel/underinspection",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportSendersExcelUnderInspectionState(final @PathVariable int year,
                                            final Locale locale) {

        return new ModelAndView(annualStatisticsWorkflowFeature.exportAnnualStatisticsSendersView(year, locale, UNDER_INSPECTION));
    }

    @PostMapping(value = "/year/{year:\\d+}/sendersexcel/approved",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportSendersExcelApprovedState(final @PathVariable int year,
                                           final Locale locale) {

        return new ModelAndView(annualStatisticsWorkflowFeature.exportAnnualStatisticsSendersView(year, locale, APPROVED));
    }
}
