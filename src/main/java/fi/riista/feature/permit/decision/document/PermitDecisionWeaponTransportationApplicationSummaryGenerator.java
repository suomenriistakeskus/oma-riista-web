package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.weapontransportation.justification.JustificationDTO;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponType;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleType;
import fi.riista.feature.permit.application.weapontransportation.reason.ReasonDTO;
import fi.riista.feature.permit.application.weapontransportation.summary.SummaryDTO;
import fi.riista.util.Locales;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PermitDecisionWeaponTransportationApplicationSummaryGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    private final SummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final EnumLocaliser localiser;

    public PermitDecisionWeaponTransportationApplicationSummaryGenerator(final SummaryDTO model,
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

        createReason(sb);
        createArea(sb);
        createJustification(sb);
        createAttachments(sb);

        return sb.toString();
    }

    public String generateApplicationReasoning() {
        final StringBuilder sb = new StringBuilder();

        final ReasonDTO reason = model.getReason();
        sb.append(i18n("Tyyppi: ", "Typ: "))
                .append(localiser.getTranslation(reason.getReasonType()));
        final String reasonDescription = reason.getReasonDescription();
        if (StringUtils.hasText(reasonDescription)) {
            sb.append(" - ").append(reasonDescription);
        }
        sb.append("\n");

        sb.append(i18n("Aika: ", "Tid: "))
                .append(DATE_FORMAT.print(reason.getBeginDate()))
                .append(" - ")
                .append(DATE_FORMAT.print(reason.getEndDate()))
                .append("\n\n");

        final JustificationDTO justification = model.getJustification();
        sb.append(i18n("Ajoneuvotiedot: ", "Fordonsinformation: ")).append("\n");
        justification.getVehicles().forEach(vehicle -> {
            final WeaponTransportationVehicleType type = vehicle.getType();
            sb.append("- ").append(localiser.getTranslation(type));

            if (type == WeaponTransportationVehicleType.MUU) {
                sb.append(" - ").append(vehicle.getDescription());
            }

            sb.append("\n");
        });
        sb.append("\n");

        sb.append(i18n("Asetiedot: ", "Vapeninformation: ")).append("\n");
        justification.getTransportedWeapons().forEach(transportedWeapon -> {
            final TransportedWeaponType type = transportedWeapon.getType();
            sb.append("- ").append(localiser.getTranslation(type));

            if (type == TransportedWeaponType.MUU) {
                sb.append(" - ").append(transportedWeapon.getDescription());
            }

            sb.append("\n");
        });
        sb.append("\n");

        sb.append(i18n("Perustelut: ", "Motiveringar: "))
                .append(justification.getJustification())
                .append("\n");

        return sb.toString();
    }

    private void createHeader(final StringBuilder sb) {
        sb.append(i18n(
                "Hakija on hakenut aseenkuljetuslupaa seuraavasti:",
                "Sökande har ansökt om tillstånd att transportera vapen enligt följande:"));
    }

    private void createReason(final StringBuilder sb) {
        final ReasonDTO reason = model.getReason();
        sb.append("\n\n")
                .append(i18n("Tyyppi ja aika:", "Typ och tid:"))
                .append("\n\n");

        sb.append("---|---:\n");
        cell2(sb, localiser.getTranslation(reason.getReasonType()),
                Optional.ofNullable(reason.getReasonDescription()).orElse(""));
        cell2(sb, DATE_FORMAT.print(reason.getBeginDate()) +
                (" - ") +
                (DATE_FORMAT.print(reason.getEndDate())),
                "");
        sb.append("\n\n");
    }

    private void createJustification(final StringBuilder sb) {
        final JustificationDTO justification = model.getJustification();

        sb.append("\n\n")
                .append(i18n("Ajoneuvo:", "Fordon:"))
                .append("\n\n");
        sb.append("---|---:\n");

        justification.getVehicles().forEach(vehicle -> {
            final WeaponTransportationVehicleType type = vehicle.getType();
            sb.append(localiser.getTranslation(type));

            if (type == WeaponTransportationVehicleType.MUU) {
                sb.append(" - ").append(vehicle.getDescription());
            }
            sb.append("\n");

        });
        sb.append("\n\n");

        sb.append("\n\n")
                .append(i18n("Asetiedot:", "Vapeninformation:"))
                .append("\n\n");
        sb.append("---|---|---:\n");
        justification.getTransportedWeapons().forEach(transportedWeapon -> {
            final TransportedWeaponType type = transportedWeapon.getType();
            sb.append(localiser.getTranslation(type));

            if (type == TransportedWeaponType.MUU) {
                sb.append(" - ").append(transportedWeapon.getDescription());
            }
            sb.append("\n");
        });
        sb.append("\n\n");

        sb.append("\n\n")
                .append(i18n("Perustelut:", "Motiveringar:"))
                .append("\n");
        sb.append(justification.getJustification())
                .append("\n\n");
    }

    private void createArea(final StringBuilder sb) {
        final DerogationPermitApplicationAreaDTO areaDTO =
                model.getDerogationPermitApplicationAreaDTO();

        sb.append("\n\n")
                .append(i18n("Hakemusalue:", "Ansökningsområde:"))
                .append("\n\n");

        final String areaDescription = areaDTO.getAreaDescription();
        if (isNotBlank(areaDescription)) {
            sb.append("---|---:\n");
            cell2(sb, i18nKey("carnivore.application.area.areaDescription"), areaDescription);
        }
        sb.append("\n\n");

        for (final DerogationPermitApplicationAttachmentDTO s : model.getAreaAttachments()) {
            sb.append("\\- ")
                    .append(s.getName())
                    .append("\n");
        }

        sb.append("\n\n");

    }

    private void createAttachments(final StringBuilder sb) {
        if (!model.getOtherAttachments().isEmpty()) {
            sb.append(i18n("Liitteet", "Bilagor"));
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

    private static void cell2(final StringBuilder sb, final String firstCell, final Object... secondCell) {
        sb.append(firstCell);
        sb.append('|');
        for (int i = 0; i < secondCell.length; i++) {
            sb.append(secondCell[i]);
            if (i < secondCell.length - 1) {
                sb.append(' ');
            }
        }
        sb.append('\n');
    }
}
