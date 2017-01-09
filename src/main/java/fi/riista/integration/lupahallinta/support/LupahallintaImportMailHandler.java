package fi.riista.integration.lupahallinta.support;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.integration.lupahallinta.HarvestPermitImportException;
import fi.riista.integration.lupahallinta.HarvestPermitImportResultDTO;
import fi.riista.util.jpa.JpaSpecs;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

@Component
public class LupahallintaImportMailHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LupahallintaImportMailHandler.class);

    private static final String MAIL_SUBJECT = "Lupien import Lupahallinnasta epäonnistui/huomioitavaa";

    private static final Set<String> OTHER_RECEIVERS = Sets.newHashSet("lupahallinto.kirjaamo@riista.fi");

    @Resource
    private MailService mailService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private RuntimeEnvironmentUtil environmentUtil;

    public void handleError(Exception e) {
        if (e instanceof HarvestPermitImportException) {
            handleImportException((HarvestPermitImportException) e);
        } else {
            handleUnknownException(e);
        }
    }

    public void handleMessages(List<String> messages) {
        String body = "Huomioitavaa lupien importissa:\n" + StringUtils.join(messages, "\n");
        LOG.warn(body);
        sendMail(body, Collections.emptySet());
    }

    private void handleImportException(HarvestPermitImportException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("CSV:n parsinnassa tapahtui virheitä ");
        sb.append(e.getAllErrors().size());
        sb.append(" kpl.\n");
        for (HarvestPermitImportResultDTO.PermitParsingError err : e.getAllErrors()) {
            sb.append("Rivi:");
            sb.append(err.getRow());

            sb.append(" Lupanumero:");
            sb.append(err.getPermitNumber());

            sb.append("  Virheet:");
            sb.append('\n');
            for (String error : err.getErrors()) {
                sb.append(error);
                sb.append('\n');
            }
            sb.append('\n');
        }
        final String body = sb.toString();
        LOG.warn(body);
        sendMail(body, collectRkaOfficialCodes(e.getAllErrors()));
    }

    private void handleUnknownException(Exception e) {
        LOG.error("Unknown error", e);
        sendMail(Throwables.getStackTraceAsString(e), Collections.emptySet());
    }

    private void sendMail(String body, Set<String> rkaCodes) {
        final Set<String> emails = listReceivers(rkaCodes);
        for (String email : emails) {
            sendMail(email, body);
        }
    }

    private Set<String> listReceivers(Set<String> rkaCodes) {
        return Sets.union(otherReceivers(rkaCodes), listSystemUserEmails()).immutableCopy();
    }

    private Set<String> listSystemUserEmails() {
        return userRepository.listHavingAnyOfRole(singletonList(SystemUser.Role.ROLE_ADMIN))
                .stream()
                .filter(SystemUser::isActive)
                .map(SystemUser::getEmail)
                .collect(toSet());
    }

    private Set<String> otherReceivers(Set<String> rkaCodes) {
        return true
                ? Sets.union(OTHER_RECEIVERS, rkaEmails(rkaCodes))
                : Collections.emptySet();
    }


    private void sendMail(String email, String body) {
        final MailMessageDTO.Builder builder = new MailMessageDTO.Builder()
                .withTo(email)
                .withSubject(MAIL_SUBJECT)
                .withBody(body);
        mailService.sendLater(builder, DateTime.now().plusMinutes(1));
    }

    // protected for tests
    protected static Set<String> collectRkaOfficialCodes(List<HarvestPermitImportResultDTO.PermitParsingError> allErrors) {
        return allErrors.stream()
                .map(e -> e.getPermitNumber())
                .map(LupahallintaImportMailHandler::resolveRkaOfficialCode)
                .collect(toSet());
    }

    private static String resolveRkaOfficialCode(String permitNumber) {
        return permitNumber.substring(7, 10);
    }

    // protected for tests
    protected Set<String> rkaEmails(Set<String> rkaCodes) {
        final Specification<Organisation> spec = JpaSpecs.and(
                JpaSpecs.equal(Organisation_.organisationType, OrganisationType.RKA),
                JpaSpecs.inCollection(Organisation_.officialCode, rkaCodes));
        return organisationRepository.findAll(spec)
                .stream()
                .map(rka -> rka.getEmail())
                .collect(toSet());
    }
}
