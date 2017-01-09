package fi.riista.feature.huntingclub.permit.partner;

import com.google.common.collect.Sets;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitTotalPaymentDTO;
import fi.riista.util.LocalisedString;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;

public class AllPartnersFinishedHuntingMailServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private AllPartnersFinishedHuntingMailService mailService;

    @Test
    public void testAllOK() {
        send(true);
    }

    @Test
    public void testNotEdiblesNotOk() {
        send(false);
    }

    private void send(final boolean notEdibleOk) {
        final HuntingClubPermitTotalPaymentDTO payment = new HuntingClubPermitTotalPaymentDTO();
        payment.setIban(Iban.random(CountryCode.FI).toFormattedString());
        payment.setBic("ASDF");
        payment.setCreditorReference(creditorReference().getValue());
        payment.setAdultsPayment(BigDecimal.valueOf(100));
        payment.setYoungPayment(BigDecimal.valueOf(100));
        payment.setDueDate(new LocalDate().plusDays(7));

        final LocalisedString speciesName = LocalisedString.of("hirvi", "ålg");

        final AllPartnersFinishedHuntingMailService.MailData data = new AllPartnersFinishedHuntingMailService.MailData(
                1, 2, "2015-123-1234567-1", speciesName, notEdibleOk, payment);
        mailService.sendEmailInternal(Sets.newHashSet("test@invalid"), data);
    }
}
