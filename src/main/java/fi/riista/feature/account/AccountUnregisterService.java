package fi.riista.feature.account;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class AccountUnregisterService {

    private static final String OMA_RIISTA_EMAIL = "oma@riista.fi";

    @Resource
    private RuntimeEnvironmentUtil environmentUtil;

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendUnregisterMail(final Person person) {

        final String omaRiistaServiceDesk = environmentUtil.isProductionEnvironment()
                ? OMA_RIISTA_EMAIL
                : OMA_RIISTA_EMAIL + ".invalid";

        mailService.send(new AccountUnregisterEmail(handlebars, messageSource)
                .withRecipient(omaRiistaServiceDesk)
                .withRecipient(person.getEmail())
                .withDate(DateUtil.now())
                .withEmail(person.getEmail())
                .withFirstName(person.getFirstName())
                .withLastName(person.getLastName())
                .withHunterNumber(person.getHunterNumber())
                .build(mailService.getDefaultFromAddress()));
    }
}
