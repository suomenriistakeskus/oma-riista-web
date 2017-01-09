package fi.riista.feature.organization.jht.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.jht.nomination.OccupationNominationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.EmailResolver;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Objects;


@Service
public class NotifyJhtOccupationNominationToRkaService {
    private static final Logger LOG = LoggerFactory.getLogger(NotifyJhtOccupationNominationToRkaService.class);

    private final static String EMAIL_TEMPLATE = "email_jht_rka_notification";

    @Resource
    private MailService mailService;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private Handlebars handlebars;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private OccupationNominationRepository occupationNominationRepository;

    @Transactional
    public void sendNotificationEmail(final LocalDate nominationDate) {
        occupationNominationRepository.findRkaNotifications(nominationDate).forEach(notification -> {

            final String occupationName = enumLocaliser.getTranslation(notification.getOccupationType(), Locales.FI);
            final String rhyName = notification.getRhyName().getAnyTranslation(Locales.FI);
            final String rkaEmail = Objects.requireNonNull(EmailResolver
                    .sanitizeEmail(notification.getRkaEmail()), "rkaEmail is null");

            final ImmutableMap.Builder<String, Object> model = ImmutableMap.<String, Object>builder()
                    .put("occupationName", occupationName)
                    .put("rhyName", rhyName)
                    .put("link", createLink(notification.getRhyOfficialCode(), notification.getOccupationType()));

            LOG.info("Sending notification for rhyCode={} occupationType={} to rkaEmail={}",
                    notification.getRhyOfficialCode(), notification.getOccupationType(), rkaEmail);

            mailService.sendLater(new MailMessageDTO.Builder()
                    .withSubject(formatSubject(occupationName, rhyName))
                    .withHandlebarsBody(handlebars, EMAIL_TEMPLATE, model.build())
                    .withTo(rkaEmail), null);
        });
    }

    private String createLink(final String rhyOfficialCode,
                              final OccupationType occupationType) {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .path("/")
                .fragment("/moderator/jht?rhyCode={rhyCode}&occupationType={occupationType}")
                .buildAndExpand(rhyOfficialCode, occupationType.name())
                .toUriString();
    }

    private static String formatSubject(final String occupationName, final String rhyName) {
        return MessageFormat.format("JHT-nimitysesitys {0} {1}", rhyName, occupationName);
    }
}
