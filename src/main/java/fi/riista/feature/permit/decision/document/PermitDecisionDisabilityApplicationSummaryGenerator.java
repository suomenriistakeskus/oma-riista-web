package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.application.PermitApplicationVehicleType;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.disability.basicinfo.BasicInfoDTO;
import fi.riista.feature.permit.application.disability.justification.HuntingType;
import fi.riista.feature.permit.application.disability.justification.JustificationDTO;
import fi.riista.feature.permit.application.disability.summary.DisabilityPermitSummaryDTO;
import fi.riista.util.Locales;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;

public class PermitDecisionDisabilityApplicationSummaryGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    private final DisabilityPermitSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final EnumLocaliser localiser;

    public PermitDecisionDisabilityApplicationSummaryGenerator(final DisabilityPermitSummaryDTO model,
                                                               final Locale locale,
                                                               final MessageSource messageSource) {
        this.model = Objects.requireNonNull(model);
        this.locale = Objects.requireNonNull(locale);
        this.swedish = Locales.isSwedish(locale);
        this.messageSource = Objects.requireNonNull(messageSource);
        this.localiser = new EnumLocaliser(messageSource, locale);
    }

    private String i18n(final String fi, final String sv) {
        return swedish ? sv : fi;
    }

    private String i18nKey(final String key) {
        return messageSource.getMessage(key, null, locale);
    }

    public String generateApplicationMain() {
        final StringBuilder sb = new StringBuilder();

        createHeader(sb);
        sb.append("\n\n");

        createBasicInfo(sb);
        createJustification(sb);
        createAttachments(sb);

        return sb.toString();
    }

    public String generateApplicationReasoning() {
        final StringBuilder sb = new StringBuilder();

        createBasicInfo(sb);
        createJustification(sb);

        return sb.toString();
    }

    private void createHeader(final StringBuilder sb) {
        sb.append(i18n(
                "Hakija on hakenut lupaa moottoriajoneuvon käytölle liikuntarajoitteisena seuraavasti:",
                "Den sökande har som rörelsehämmad ansökt om dispens för användning av motordrivet fordon enligt följande:"));
    }

    private void createBasicInfo(final StringBuilder sb) {
        final BasicInfoDTO basicInfo = model.getBasicInfo();
        sb.append("\n\n")
                .append(i18n("Tyyppi:", "Typ:"))
                .append("\n\n");

        if (basicInfo.getUseMotorVehicle()) {
            sb.append("- ").append(i18nKey("disability.application.useMotorVehicle")).append("\n");
        }
        if (basicInfo.getUseVehicleForWeaponTransport()) {
            sb.append("- ").append(i18nKey("disability.application.useVehicleForWeaponTransport")).append("\n");
        }
        sb.append("\n");

        sb.append(i18n("Aika: ", "Tid: "))
                .append(DATE_FORMAT.print(basicInfo.getBeginDate()))
                .append(" - ")
                .append(DATE_FORMAT.print(basicInfo.getEndDate()));
    }

    private void createJustification(final StringBuilder sb) {
        final JustificationDTO justification = model.getJustification();

        sb.append("\n\n")
                .append(i18n("Ajoneuvotiedot:", "Fordoninformation:"))
                .append("\n\n");

        justification.getVehicles().forEach(vehicle -> {
            final PermitApplicationVehicleType type = vehicle.getType();
            sb.append("- ").append(localiser.getTranslation(type));

            if (type == PermitApplicationVehicleType.MUU) {
                sb.append(" - ").append(escape(vehicle.getDescription()));
            }
            sb.append("\n");

            sb.append(escape(vehicle.getJustification())).append("\n");

        });

        sb.append("\n\n")
                .append(i18n("Metsästysmuodot:", "Jaktformer:"))
                .append("\n\n");
        justification.getHuntingTypeInfos().forEach(huntingTypeInfo ->  {
            final HuntingType huntingType = huntingTypeInfo.getHuntingType();
            sb.append("- ").append(localiser.getTranslation(huntingType));

            if (huntingType == HuntingType.MUU) {
                sb.append(" - ").append(escape(huntingTypeInfo.getHuntingTypeDescription()));
            }

            sb.append("\n");
        });

        sb.append("\n\n")
                .append(i18n("Perustelut:", "Motiveringar:"))
                .append("\n");
        sb.append(escape(justification.getJustification()));
    }

    private void createAttachments(final StringBuilder sb) {
        if (!model.getOtherAttachments().isEmpty()) {
            sb.append("\n\n").append(i18n("Liitteet", "Bilagor"));
            sb.append("\n");

            for (DerogationPermitApplicationAttachmentDTO s : model.getOtherAttachments()) {
                sb.append("\\- ");
                sb.append(s.getName());
                sb.append("\n");

                if (StringUtils.hasText(s.getAdditionalInfo())) {
                    sb.append(escape(s.getAdditionalInfo()))
                            .append("\n");
                }
            }

            sb.append("\n\n");
        }
    }
}
