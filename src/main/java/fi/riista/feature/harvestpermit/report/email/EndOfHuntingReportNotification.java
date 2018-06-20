package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.Maps;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitSpecimenSummary;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EndOfHuntingReportNotification {
    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_harvest_report_end_of_hunting", "email_harvest_report_end_of_hunting.sv");

    private final Handlebars handlebars;
    private final MessageSource messageSource;
    private HarvestReportState state;
    private Map<GameSpecies, HarvestPermitSpecimenSummary> summaries;
    private List<Harvest> harvests;

    public EndOfHuntingReportNotification(final Handlebars handlebars, final MessageSource messageSource) {
        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    private Set<String> recipients;
    private PersonContactInfoDTO author;
    private String permitNumber;

    public EndOfHuntingReportNotification withPermit(@Nonnull final HarvestPermit permit) {
        Objects.requireNonNull(permit, "permit is null");

        this.permitNumber = permit.getPermitNumber();
        this.author = PersonContactInfoDTO.create(Objects.requireNonNull(permit.getHarvestReportAuthor()));
        this.state = permit.getHarvestReportState();
        this.harvests = permit.getAcceptedHarvestForEndOfHuntingReport();

        return this;
    }

    public EndOfHuntingReportNotification withSummaries(Map<GameSpecies, HarvestPermitSpecimenSummary> summaries) {
        this.summaries = summaries;
        return this;
    }

    public EndOfHuntingReportNotification withRecipients(Set<String> recipients) {
        this.recipients = recipients;

        return this;
    }

    public MailMessageDTO build(final String emailFrom) {
        Objects.requireNonNull(this.permitNumber, "No permit");
        Objects.requireNonNull(this.author, "No person");
        Objects.requireNonNull(this.summaries, "No summaries");

        final String emailSubject = messageSource.getMessage("harvest.report.email.subject", null, Locales.FI);

        final HashMap<String, Object> model = Maps.newHashMap();
        model.put("timestamp", DateTime.now().toDate());
        model.put("author", this.author);
        model.put("permitNumber", this.permitNumber);
        model.put("state", this.state);
        model.put("noHarvest", this.summaries.keySet().isEmpty());
        model.put("summaries", this.summaries);
        model.put("harvests", this.harvests);

        return MailMessageDTO.builder()
                .withFrom(emailFrom)
                .withRecipients(this.recipients)
                .withSubject(emailSubject)
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .appendBody("</body></html>")
                .build();
    }
}
