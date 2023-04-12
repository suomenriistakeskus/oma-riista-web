package fi.riista.feature.permit.decision.informationrequest;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.Maps;
import fi.riista.api.pub.PublicDecisionRequestOfInformationApiResource;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;


public class InformationRequestEmail {

    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_information_request", "email_information_request.sv");

    private final Handlebars handlebars;
    private final RuntimeEnvironmentUtil runtimeEnvironmentUtil;


    private DateTime validUntil;
    private InformationRequestLink link;
    private String permitNumber;

    private Set<String> recipients;

    public InformationRequestEmail(
            final Handlebars handlebars,
            final RuntimeEnvironmentUtil runtimeEnvironmentUtil) {
        this.handlebars = handlebars;
        this.runtimeEnvironmentUtil = runtimeEnvironmentUtil;
    }

    public MailMessageDTO build(final String emailFrom) {
        Objects.requireNonNull(this.validUntil, "Valid until datetime isn't set");
        Objects.requireNonNull(this.link, "No link set");
        Objects.requireNonNull(this.permitNumber, "No permit number set");

        final String emailSubject = this.link.getTitle();

        final HashMap<String, Object> model = Maps.newHashMap();
        model.put("link", getLinkUrl());
        model.put("validUntil", this.validUntil);
        model.put("message", this.link.getDescription());


        return MailMessageDTO.builder()
                .withFrom(emailFrom)
                .withSubject(emailSubject)
                .withRecipients(recipients)
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("\n\n--------------------------------------------------------------------------------\n\n")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .build();
    }

    public InformationRequestEmail withLink(final InformationRequestLink link) {
        this.link = link;
        return this;
    }

    public InformationRequestEmail withValidUntil(final DateTime validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public InformationRequestEmail withPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
        return this;
    }

    public InformationRequestEmail withRecipients(final Set<String> recipients) {
        this.recipients = recipients;

        return this;
    }

    private String getLinkUrl() {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("{prefix}/{linkKey}/{documentNumber}")
                .buildAndExpand(PublicDecisionRequestOfInformationApiResource.API_PREFIX,
                        this.link.getLinkIdentifier(),
                        this.permitNumber)
                .toUri().toString();
    }

}
