package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.dogevent.disturbance.DogEventDisturbanceDTO;
import fi.riista.feature.permit.application.dogevent.summary.DogEventDisturbanceSummaryDTO;
import org.springframework.context.MessageSource;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;

import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.beginAndEndDate;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.join;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.joinNotBlankBy;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.minorTitle;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.minorTitleLine;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.table2;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.titleLine;
import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.unorderedList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PermitDecisionDogDisturbanceApplicationSummaryGenerator {

    private final DogEventDisturbanceSummaryDTO dto;
    private final Locale locale;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionDogDisturbanceApplicationSummaryGenerator(
            @Nonnull final DogEventDisturbanceSummaryDTO dto,
            @Nonnull final Locale locale,
            @Nonnull final Map<Integer, String> speciesNameIndex,
            @Nonnull final MessageSource messageSource) {

        this.dto = requireNonNull(dto);
        this.locale = requireNonNull(locale);
        this.messageSource = requireNonNull(messageSource);
        this.speciesNameIndex = requireNonNull(speciesNameIndex);
    }

    public String generateApplicationMain() {
        final StringBuilder sb = new StringBuilder();

        sb.append(i18nKey("dogevent.application.intro"));
        sb.append("\n\n");

        appendEventInfo(sb);
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

    private void appendEventInfo(final StringBuilder sb) {
        titleLine(sb, i18nKey("dogevent.application.heading.trainingEvent"));
        eventInfo(sb, dto.getTrainingEvent(), "dogevent.application.event.noTrainingEvent");

        titleLine(sb, i18nKey("dogevent.application.heading.testEvent"));
        eventInfo(sb, dto.getTestEvent(), "dogevent.application.event.noTestEvent");
    }

    private void eventInfo(final StringBuilder sb, final DogEventDisturbanceDTO eventDto, final String noEventTextKey) {
        sb.append(eventDto.isSkipped() ? i18nKey(noEventTextKey) : generateEventInfo(eventDto));
        sb.append("\n\n");
    }

    private StringBuilder generateEventInfo(final DogEventDisturbanceDTO eventDto) {
        final StringBuilder sb = new StringBuilder();

        table2(sb,
               minorTitle(i18nKey("dogevent.application.event.date")), beginAndEndDate(eventDto.getBeginDate(), eventDto.getEndDate()),
               minorTitle(i18nKey("species")), speciesNameIndex.get((eventDto.getSpeciesCode())),
               minorTitle(i18nKey("dogevent.application.event.dogsAmount")), eventDto.getDogsAmount());

        minorTitleLine(sb, i18nKey("dogevent.application.heading.contacts"));
        eventDto.getContacts().forEach(contact -> {
            unorderedList(sb, joinNotBlankBy(", ", contact.getName(), contact.getPhone(), contact.getMail()));
        });
        sb.append("\n\n");

        minorTitleLine(sb, i18nKey("dogevent.application.event.description"));
        sb.append(eventDto.getEventDescription());

        return sb;
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
        table2(sb, minorTitle(i18nKey("carnivore.application.area.size")), join(areaDto.getAreaSize(), i18nKey("pdf.application.ha")));

        final String areaDescription = areaDto.getAreaDescription();
        if (isNotBlank(areaDescription)) {
            minorTitleLine(sb, i18nKey("carnivore.application.area.areaDescription"));
            sb.append(areaDescription);
            sb.append("\n\n");
        }

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
