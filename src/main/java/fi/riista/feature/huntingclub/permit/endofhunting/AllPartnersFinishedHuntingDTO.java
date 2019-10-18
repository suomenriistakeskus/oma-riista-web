package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class AllPartnersFinishedHuntingDTO {
    private static final LocalisedString TEMPLATE_SUBJECT = new LocalisedString(
            "Luvan osakkaat ovat päättäneet metsästyksen %s (%s)",
            "Alla delägare i jaktlicensen %s för %s har avslutat jakten");

    private static final LocalisedString TEMPLATE_BODY = new LocalisedString(
            "email_all_partners_finished_hunting", "email_all_partners_finished_hunting.sv");

    public static AllPartnersFinishedHuntingDTO create(final @Nonnull HarvestPermit harvestPermit,
                                                       final @Nonnull GameSpecies gameSpecies,
                                                       final @Nonnull URI dashboardUri,
                                                       final @Nonnull Locale locale) {
        final String speciesName = gameSpecies.getNameLocalisation().getTranslation(locale);
        final String emailSubject = String.format(TEMPLATE_SUBJECT.getTranslation(locale), harvestPermit.getPermitNumber(), speciesName);
        final String emailTemplate = TEMPLATE_BODY.getTranslation(locale);

        return new AllPartnersFinishedHuntingDTO(harvestPermit.getPermitNumber(), speciesName, emailTemplate,
                emailSubject, dashboardUri);
    }

    private final String permitNumber;
    private final String speciesName;
    private final String mailTemplate;
    private final String mailSubject;
    private final URI dashboardUri;

    private AllPartnersFinishedHuntingDTO(final @Nonnull String permitNumber,
                                          final @Nonnull String speciesName,
                                          final @Nonnull String mailTemplate,
                                          final @Nonnull String mailSubject,
                                          final @Nonnull URI dashboardUri) {
        this.permitNumber = requireNonNull(permitNumber);
        this.speciesName = requireNonNull(speciesName);
        this.mailTemplate = requireNonNull(mailTemplate);
        this.mailSubject = requireNonNull(mailSubject);
        this.dashboardUri = requireNonNull(dashboardUri);
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public String getMailTemplate() {
        return mailTemplate;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public URI getDashboardUri() {
        return dashboardUri;
    }
}
