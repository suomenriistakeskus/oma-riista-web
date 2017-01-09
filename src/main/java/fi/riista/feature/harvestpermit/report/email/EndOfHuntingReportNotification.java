package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EndOfHuntingReportNotification {
    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_harvest_report_end_of_hunting", "email_harvest_report_end_of_hunting.sv");

    private final Handlebars handlebars;
    private final MessageSource messageSource;
    private Long id;
    private Integer rev;
    private HarvestReport.State state;
    private HarvestReport report;
    private Map<GameSpecies, SpecimenSummary> summaries;

    public EndOfHuntingReportNotification(final Handlebars handlebars, final MessageSource messageSource) {
        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    private String email;
    private PersonDTO author;
    private HarvestPermitDTO harvestPermit;

    private Long createdByUserId;
    private Long modifiedByUserId;

    public EndOfHuntingReportNotification withReport(HarvestReport report) {
        this.report = report;
        this.author = PersonDTO.create(Objects.requireNonNull(report.getAuthor()));
        this.createdByUserId = report.getCreatedByUserId();
        this.modifiedByUserId = report.getModifiedByUserId();

        this.id = report.getId();
        this.rev = report.getConsistencyVersion();
        this.state = report.getState();

        return this;
    }

    public EndOfHuntingReportNotification withPermit(final HarvestPermit permit) {
        if (permit != null) {
            this.harvestPermit = HarvestPermitDTO.create(permit, null, EnumSet.of(HarvestPermitDTO.Inclusion.REPORT_LIST));
        }

        return this;
    }

    public EndOfHuntingReportNotification withSummaries(Map<GameSpecies, SpecimenSummary> summaries) {
        this.summaries = summaries;
        return this;
    }

    public EndOfHuntingReportNotification withEmail(String email) {
        this.email = email;

        return this;
    }

    public MailMessageDTO.Builder build() {
        Objects.requireNonNull(this.harvestPermit, "No permit");
        Objects.requireNonNull(this.author, "No person");
        Objects.requireNonNull(this.summaries, "No summaries");
        Preconditions.checkState(StringUtils.hasText(this.email), "No email address");

        final String emailSubject = messageSource.getMessage("harvest.report.email.subject", null, Locales.FI);

        final HashMap<String, Object> model = Maps.newHashMap();
        model.put("timestamp", new Date());
        model.put("report", report);
        model.put("author", this.author);
        model.put("permit", this.harvestPermit);
        model.put("createdByUserId", this.createdByUserId);
        model.put("modifiedByUserId", this.modifiedByUserId);
        model.put("id", this.id);
        model.put("rev", this.rev);
        model.put("state", this.state);
        model.put("noHarvest", this.summaries.keySet().isEmpty());
        model.put("summaries", this.summaries);


        return new MailMessageDTO.Builder()
                .withTo(this.email)
                .withSubject(emailSubject)
                .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("<hr/>")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .appendBody("</body></html>");
    }

    public static class SpecimenSummary {

        private int total = 0;
        private Map<Object, Integer> counts = Maps.newHashMap();

        public static Map<GameSpecies, SpecimenSummary> create(Set<HarvestReport> reports) {
            Map<GameSpecies, SpecimenSummary> map = Maps.newHashMap();

            for (HarvestReport report : reports) {
                for (Harvest h : report.getHarvests()) {
                    GameSpecies g = h.getSpecies();
                    if (!map.containsKey(g)) {
                        map.put(g, new SpecimenSummary());
                    }
                    SpecimenSummary sum = map.get(g);
                    sum.total += h.getAmount();
                    for (HarvestSpecimen sp : h.getSortedSpecimens()) {
                        sum.update(sp.getAge());
                        sum.update(sp.getGender());
                    }
                }
            }
            return map;
        }

        private SpecimenSummary() {
        }

        private void update(Object key) {
            if (key == null) {
                return;
            }
            if (!counts.containsKey(key)) {
                counts.put(key, 0);
            }
            counts.put(key, counts.get(key) + 1);
        }

        private int get(Object key) {
            return counts.containsKey(key) ? counts.get(key) : 0;
        }

        public int getTotal() {
            return total;
        }

        public int getGenderFemale() {
            return get(GameGender.FEMALE);
        }

        public int getGenderMale() {
            return get(GameGender.MALE);
        }

        public int getGenderUnknown() {
            return get(GameGender.UNKNOWN);
        }

        public int getAgeAdult() {
            return get(GameAge.ADULT);
        }

        public int getAgeYoung() {
            return get(GameAge.YOUNG);
        }

        public int getAgeUnknown() {
            return get(GameAge.UNKNOWN);
        }
    }
}
