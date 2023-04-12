package fi.riista.feature.permit.decision.publish;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.pdf.AnnualRenewalPermitPdfFeature;
import fi.riista.feature.permit.decision.derogation.pdf.AnnualRenewalPermitPdfModelDTO;
import fi.riista.util.LocalisedString;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

import static fi.riista.util.DateUtil.now;
import static java.util.Collections.singletonList;

@Service
public class AnnualPermitRenewalNotificationService {

    private static final LocalisedString TEMPLATE = LocalisedString.of(
            "annual_renewal_notification", "annual_renewal_notification.sv");

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("d.M.yyyy");

    private static final String SUBJECT_FI = "Ilmoitusmenettely";
    private static final String SUBJECT_SV = "Anmälningsförfarande";

    @Resource
    private Handlebars handlebars;

    @Resource
    private MailService mailService;

    @Resource
    private RuntimeEnvironmentUtil environmentUtil;

    @Resource
    private AnnualRenewalPermitPdfFeature renewalPermitPdfFeature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;


    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void notifyPermitRenewal(final long permitId) {
        final HarvestPermit permit = harvestPermitRepository.getOne(permitId);
        final String email = permit.getOriginalContactPerson().getEmail();
        if (StringUtils.isBlank(email)) {
            return;
        }

        final AnnualRenewalPermitPdfModelDTO pdfModel = renewalPermitPdfFeature.getModel(permitId);

        final Map<String, Object> model = ImmutableMap.<String, Object>builderWithExpectedSize(6)
                .put("renewalDate", now().toString(FORMATTER))
                .put("decisionDate", pdfModel.getDecisionDate().toString(FORMATTER))
                .put("decisionNumber", pdfModel.getDecisionNumber())
                .put("permitId", permitId)
                .put("permitYear", pdfModel.getPermitYear())
                .put("permitNumber", pdfModel.getPermitNumber())
                .put("serverUri", environmentUtil.getBackendBaseUri())
                .build();

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(singletonList(email))
                .withSubject(String.format("%s / %s", SUBJECT_FI, SUBJECT_SV))
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .appendBody("</body></html>")
                .build());
    }

}
