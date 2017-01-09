package fi.riista.feature.huntingclub.permit.partner;


import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitTotalPaymentDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.util.LocalisedString;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@Service
public class AllPartnersFinishedHuntingMailService {

    private static final Logger LOG = LoggerFactory.getLogger(AllPartnersFinishedHuntingMailService.class);
    private static final LocalisedString EMAIL_TEMPLATE = new LocalisedString(
            "email_all_partners_finished_hunting", "email_all_partners_finished_hunting.sv");

    public static class MailData {
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy");

        private final long permitHolderId;
        private final long permitId;
        private final String permitNumber;
        private final LocalisedString speciesName;
        private final boolean allOk;
        private final boolean nokNotEdibles;
        private final String iban;
        private final String bic;
        private final String creditorReference;
        private final String dueDate;
        private final BigDecimal sum;

        public MailData(final long permitHolderId, final long permitId, final String permitNumber, final LocalisedString speciesName,
                        final boolean notEdibleOk,
                        final HuntingClubPermitTotalPaymentDTO payment) {
            this.permitHolderId = permitHolderId;
            this.permitId = permitId;
            this.permitNumber = permitNumber;
            this.speciesName = speciesName;
            this.allOk = notEdibleOk;
            this.nokNotEdibles = !notEdibleOk;
            this.iban = payment.getIban();
            this.bic = payment.getBic();
            this.creditorReference = payment.getCreditorReference();
            this.dueDate = DATE_FORMAT.print(payment.getDueDate());
            this.sum = payment.getTotalPayment();
        }

        public long getPermitHolderId() {
            return permitHolderId;
        }

        public long getPermitId() {
            return permitId;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public LocalisedString getSpeciesName() {
            return speciesName;
        }

        public boolean isAllOk() {
            return allOk;
        }

        public boolean isNokNotEdibles() {
            return nokNotEdibles;
        }

        public String getIban() {
            return iban;
        }

        public String getBic() {
            return bic;
        }

        public String getCreditorReference() {
            return creditorReference;
        }

        public String getDueDate() {
            return dueDate;
        }

        public BigDecimal getSum() {
            return sum;
        }
    }

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmailAsync(final Set<String> emails, final MailData data) {
        sendEmailInternal(emails, data);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmailInternal(final Set<String> emails, final MailData data) {
        final URI link = UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .path("/")
                .fragment("/club/{permitHolderId}/permit/{permitId}/show")
                .buildAndExpand(data.permitHolderId, data.permitId)
                .toUri();

        final Map<String, Object> model = ImmutableMap.<String, Object> builder()
                .put("data", data)
                .put("link", link)
                .build();

        final String title = String.format("Luvan osakkaat ovat päättäneet metsästyksen %s (%s)",
                data.permitNumber, data.speciesName.getFinnish());

        final MailMessageDTO.Builder builder = new MailMessageDTO.Builder()
                .withSubject(title)
                .withHandlebarsBody(handlebars, EMAIL_TEMPLATE.getFinnish(), model)
                .appendBody("\n<br/><hr/><br/>\n")
                .appendHandlebarsBody(handlebars, EMAIL_TEMPLATE.getSwedish(), model);

        for (final String email : emails) {
            LOG.info("Sending notification to {}", email);

            builder.withTo(email);
            mailService.sendLater(builder, null);
        }
    }
}
