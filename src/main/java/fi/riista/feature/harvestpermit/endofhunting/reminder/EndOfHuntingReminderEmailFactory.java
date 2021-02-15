package fi.riista.feature.harvestpermit.endofhunting.reminder;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class EndOfHuntingReminderEmailFactory {

    private static final String FIRST_REMINDER_SUBJECT_KEY = "harvestpermit.endofhunting.first.reminder.subject";
    private static final String SECOND_REMINDER_SUBJECT_KEY = "harvestpermit.endofhunting.second.reminder.subject";

    enum EndOfHuntingReminderEmailType {
        FIRST_REMINDER,
        SECOND_REMINDER
    }

    public class EndOfHuntingReminderEmailModel {
        private final String permitNumber;
        private final String speciesNames;
        private final URI dashboardUri;

        public EndOfHuntingReminderEmailModel(final String permitNumber, final String speciesNames, final URI dashboardUri) {
            this.permitNumber = permitNumber;
            this.speciesNames = speciesNames;
            this.dashboardUri = dashboardUri;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public String getSpeciesNames() {
            return speciesNames;
        }

        public String getDashboardUri() {
            return dashboardUri.toString();
        }
    }

    @Resource
    private Handlebars handlebars;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    @Resource
    private MessageSource messageSource;

    @Nonnull
    public EndOfHuntingReminderEmail build(final EndOfHuntingReminderEmailType emailType,
                                           final EndOfHuntingReminderDTO missingReport) {
        final URI dashboardUri = permitClientUriFactory.getAbsolutePermitDashboardUri(missingReport.getPermitId());

        final String gameSpeciesFinnish = StringUtils.join(
                F.mapNonNullsToList(missingReport.getGameSpecies(), LocalisedString::getFinnish),
                ",");
        final EndOfHuntingReminderEmailModel modelFi =
                new EndOfHuntingReminderEmailModel(
                        missingReport.getPermitNumber(),
                        gameSpeciesFinnish,
                        dashboardUri);

        final String gameSpeciesSwedish = StringUtils.join(
                F.mapNonNullsToList(missingReport.getGameSpecies(), LocalisedString::getSwedish),
                ",");
        final EndOfHuntingReminderEmailModel modelSv =
                new EndOfHuntingReminderEmailModel(
                        missingReport.getPermitNumber(),
                        gameSpeciesSwedish,
                        dashboardUri);

        String subject;
        String body_fi;
        String body_sv;
        try {
            switch (emailType) {
                case FIRST_REMINDER:
                    subject = messageSource.getMessage(FIRST_REMINDER_SUBJECT_KEY, null, Locales.FI) +
                            " / " +
                            messageSource.getMessage(FIRST_REMINDER_SUBJECT_KEY, null, Locales.SV);
                    body_fi = handlebars.compile("email_end_of_hunting_report_first_reminder").apply(modelFi);
                    body_sv = handlebars.compile("email_end_of_hunting_report_first_reminder_sv").apply(modelSv);
                    break;
                case SECOND_REMINDER:
                    subject = messageSource.getMessage(SECOND_REMINDER_SUBJECT_KEY, null, Locales.FI) +
                            " / " +
                            messageSource.getMessage(SECOND_REMINDER_SUBJECT_KEY, null, Locales.SV);
                    body_fi = handlebars.compile("email_end_of_hunting_report_second_reminder").apply(modelFi);
                    body_sv = handlebars.compile("email_end_of_hunting_report_second_reminder_sv").apply(modelSv);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown email type");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not render template", e);
        }
        final String body = body_fi + "\n<hr/>\n" + body_sv;

        final Set<String> recipients = new HashSet<>();
        Optional.ofNullable(missingReport.getContactPersonEmail()).ifPresent(recipients::add);
        Optional.ofNullable(missingReport.getAdditionalContactEmails()).ifPresent(recipients::addAll);

        return new EndOfHuntingReminderEmail(subject, body, recipients);
    }
}
