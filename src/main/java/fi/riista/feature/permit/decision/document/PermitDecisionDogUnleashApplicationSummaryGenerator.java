package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.dogevent.summary.DogEventUnleashSummaryDTO;
import fi.riista.feature.permit.application.dogevent.unleash.DogEventUnleashDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Objects;

import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.beginAndEndDate;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.join;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.joinNotBlankBy;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.minorTitle;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.minorTitleLine;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.optionalContent;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.table2;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.titleLine;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.unorderedList;

public class PermitDecisionDogUnleashApplicationSummaryGenerator {

    private final DogEventUnleashSummaryDTO dto;
    private final Locale locale;
    private final MessageSource messageSource;

    public PermitDecisionDogUnleashApplicationSummaryGenerator(
            @Nonnull final DogEventUnleashSummaryDTO dto,
            @Nonnull final Locale locale,
            @Nonnull final MessageSource messageSource) {

        this.dto = Objects.requireNonNull(dto);
        this.locale = Objects.requireNonNull(locale);
        this.messageSource = Objects.requireNonNull(messageSource);
    }

    public String generateApplicationMain() {
        final StringBuilder sb = new StringBuilder();

        sb.append(i18nKey("dogevent.application.intro")).append("\n\n");

        titleLine(sb, i18nKey("dogevent.application.heading.events"));
        dto.getEvents().forEach(eventDto -> appendEventInfo(sb, eventDto));

        appendAreaInfo(sb);
        appendAttachmentsInfo(sb);

        return sb.toString();
    }

    public String generateApplicationReasoning() {
        // Will be left out from the decision if empty.
        return "";
    }

    /**
     *  Event info
     */

    private void appendEventInfo(final StringBuilder sb, final DogEventUnleashDTO eventDto) {

        titleLine(sb, join(i18nKey("dogevent.application.event.type." + eventDto.getEventType().name()),
                           " ", beginAndEndDate(eventDto.getBeginDate(), eventDto.getEndDate())));
        sb.append("\n\n");

        table2(sb,
               minorTitle(i18nKey("dogevent.application.event.dogsAmount")), eventDto.getDogsAmount(),
               minorTitle(i18nKey("dogevent.application.event.geoLocation")), join(i18nKey("mooselike.application.amendment.latitude"),
                                                                                   " ", eventDto.getGeoLocation().getLatitude(),
                                                                                   " ", i18nKey("mooselike.application.amendment.longitude"),
                                                                                   " ", eventDto.getGeoLocation().getLongitude()),
               minorTitle(i18nKey("dogevent.application.event.naturaArea")), StringUtils.isNotBlank(eventDto.getNaturaArea())
                       ? eventDto.getNaturaArea() : i18nKey("dogevent.application.event.notInNaturaArea"),
               minorTitle(i18nKey("dogevent.application.event.contact")), joinNotBlankBy(", ",
                                                                                         eventDto.getContactName(),
                                                                                         eventDto.getContactPhone(),
                                                                                         eventDto.getContactMail()));

        sb.append("\n\n");

        minorTitleLine(sb, i18nKey("dogevent.application.event.description"));
        sb.append(eventDto.getEventDescription()).append("\n\n");

        minorTitleLine(sb, i18nKey("dogevent.application.event.locationDescription"));
        sb.append(eventDto.getLocationDescription()).append("\n\n");

        optionalContent(sb,
                        join(minorTitle(i18nKey("dogevent.application.event.additionalInfo")), "\n"),
                        eventDto.getAdditionalInfo(),
                        "\n\n");

    }

    private String i18nKey(final String key) {
        return messageSource.getMessage(key, null, locale);
    }

    /**
     *  Area info
     */

    private StringBuilder appendAreaInfo(final StringBuilder sb) {
        final DerogationPermitApplicationAreaDTO areaDto = dto.getDerogationPermitApplicationAreaDTO();

        titleLine(sb, i18nKey("carnivore.application.heading.area"));
        table2(sb, minorTitle(i18nKey("carnivore.application.area.size")), join(areaDto.getAreaSize(), " ", i18nKey("pdf.application.ha")));

        optionalContent(sb,
                        join(minorTitle(i18nKey("carnivore.application.area.areaDescription")), "\n"),
                        areaDto.getAreaDescription(),
                        "\n\n");

        dto.getAreaAttachments().forEach(s -> unorderedList(sb, s.getName()));
        sb.append("\n\n");

        return sb;
    }

    /**
     *  Attachments info
     */

    private void appendAttachmentsInfo(final StringBuilder sb) {
        if (!dto.getOtherAttachments().isEmpty()) {
            titleLine(sb, i18nKey("bird.application.heading.attachments"));

            dto.getOtherAttachments().forEach(attachment -> {
                unorderedList(sb, joinNotBlankBy(": ", attachment.getName(), escape(attachment.getAdditionalInfo())));
            });
        }
    }

}
