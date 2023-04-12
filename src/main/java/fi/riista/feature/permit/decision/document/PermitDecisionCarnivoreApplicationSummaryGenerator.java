package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.carnivore.CarnivorePermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.carnivore.attachments.CarnivorePermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.carnivore.justification.CarnivorePermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitApplicationSpeciesAmountDTO;
import fi.riista.util.Locales;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static org.apache.commons.lang.StringUtils.isNotBlank;


public class PermitDecisionCarnivoreApplicationSummaryGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("d.M.YYYY");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final CarnivorePermitApplicationSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionCarnivoreApplicationSummaryGenerator(final CarnivorePermitApplicationSummaryDTO model,
                                                              final Locale locale,
                                                              final Map<Integer, String> speciesNameIndex,
                                                              final MessageSource messageSource) {
        this.model = Objects.requireNonNull(model);
        this.locale = Objects.requireNonNull(locale);
        this.swedish = Locales.isSwedish(locale);
        this.messageSource = Objects.requireNonNull(messageSource);
        this.speciesNameIndex = Objects.requireNonNull(speciesNameIndex);
    }

    private String speciesName(final int gameSpeciesCode) {
        return speciesNameIndex.get(gameSpeciesCode);
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

        createAmounts(sb);
        createPeriods(sb);
        createArea(sb);
        createAttachments(sb);

        return sb.toString();
    }

    public String generateApplicationReasoning() {
        final StringBuilder sb = new StringBuilder();

        createJustification(sb);

        return sb.toString();
    }

    private void createHeader(final StringBuilder sb) {

        sb.append(i18n(
                "Hakija on hakenut poikkeuslupaa seuraavasti:",
                "Sökande har ansökt om dispenslicens enligt följande:"));

    }

    private void createAmounts(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja lupamäärä:",
                "Djurart och licensantal:"));
        sb.append("\n\n");
        sb.append("---|---:\n");

        final CarnivorePermitApplicationSpeciesAmountDTO speciesAmount = model.getSpeciesAmounts();
        sb.append(speciesName(speciesAmount.getGameSpeciesCode()));
        sb.append('|');
        sb.append(DECIMAL_FORMAT.format(speciesAmount.getAmount()));
        sb.append(' ');
        sb.append(i18n("kpl", "st."));
        sb.append('\n');

    }

    private void createPeriods(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja haettu lupa-aika:",
                "Djurart och ansökta giltighetstiden:"));
        sb.append("\n\n");
        sb.append("---|---:\n");

        final CarnivorePermitApplicationSpeciesAmountDTO speciesAmount = model.getSpeciesAmounts();
        sb.append(speciesName(speciesAmount.getGameSpeciesCode()));
        sb.append('|');
        sb.append(DATE_FORMAT.print(speciesAmount.getBegin()));
        sb.append(" - ");
        sb.append(DATE_FORMAT.print(speciesAmount.getEnd()));
        sb.append('\n');

    }

    private void createArea(final StringBuilder sb) {
        sb.append("\n\n")
                .append(i18n("Hakemusalue:", "Ansökningsområde:"))
                .append("\n\n")
                .append("---|---:\n");
        cell2(sb, i18nKey("carnivore.application.area.size"), "" + model.getAreaSize() + " ha");
        final String areaDescription = model.getAreaDescription();
        if (isNotBlank(areaDescription)) {
            cell2(sb, i18nKey("carnivore.application.area.areaDescription"), areaDescription);
        }
        sb.append("\n\n");

        for (final CarnivorePermitApplicationAttachmentDTO s : model.getAreaAttachments()) {
            sb.append("\\- ")
                    .append(s.getName())
                    .append("\n");
        }
        sb.append("\n\n");
    }


    private void createJustification(final StringBuilder sb) {
        final CarnivorePermitApplicationJustificationDTO justification = model.getJustification();

        sb.append(i18nKey("carnivore.application.justification.population"));
        sb.append("\n");
        sb.append("\\- ");
        sb.append(escape(justification.getPopulationAmount()));
        sb.append("\n");

        sb.append("\n");
        sb.append(i18nKey("carnivore.application.justification.alternatives"));
        sb.append("\n");
        sb.append("\\- ");
        sb.append(escape(justification.getAlternativeMeasures()));
        sb.append("\n");
        sb.append("\n");
    }

    private void createAttachments(final StringBuilder sb) {
        if (model.getOtherAttachments().size() > 0) {
            sb.append(i18n("Liitteet", "Bilagor"));
            sb.append("\n");

            for (final CarnivorePermitApplicationAttachmentDTO s : model.getOtherAttachments()) {
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
