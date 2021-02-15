package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.importing.amount.ImportingPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.importing.justification.ImportingPermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.importing.period.ImportingPermitApplicationSpeciesPeriodDTO;
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


public class PermitDecisionImportingApplicationSummaryGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("d.M.YYYY");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final ImportingPermitApplicationSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionImportingApplicationSummaryGenerator(final ImportingPermitApplicationSummaryDTO model,
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

        sb.append(i18nKey("bird.application.period.limited"));
        sb.append(' ');
        sb.append(model.getValidityYears());
        sb.append(' ');
        sb.append(i18nKey("bird.application.period.years"));
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
                "Hakija on hakenut maahantuontilupaa seuraavasti:",
                "Hakija on hakenut maahantuontilupaa seuraavasti:"));

    }

    private void createAmounts(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja lupamäärä:",
                "Djurart och licensantal:"));
        sb.append("\n\n");
        sb.append("---|---|---:\n");

        for (final ImportingPermitApplicationSpeciesAmountDTO speciesAmount : model.getSpeciesAmounts()) {
            final Integer specimenAmount = speciesAmount.getSpecimenAmount();
            final Integer eggAmount = speciesAmount.getEggAmount();

            sb.append(speciesName(speciesAmount.getGameSpeciesCode()));
            if (StringUtils.hasText(speciesAmount.getSubSpeciesName())){
                sb.append(String.format(" (%s)", escape(speciesAmount.getSubSpeciesName())));
            }
            sb.append("|");
            if (specimenAmount != null) {
                sb.append(DECIMAL_FORMAT.format(specimenAmount));
                sb.append(' ');
                sb.append(i18n("yksilöä", "individer"));
            }
            sb.append("|");
            if (eggAmount != null) {
                sb.append(DECIMAL_FORMAT.format(eggAmount));
                sb.append(' ');
                sb.append(i18n("munaa", "ägg"));
            }
            sb.append("\n");
        }
    }


    private void createPeriods(final StringBuilder sb) {
        sb.append("\n\n")
                .append(i18n(
                        "Eläinlaji ja haettu lupa-aika:",
                        "Djurart och ansökta giltighetstiden:"))
                .append("\n\n");

        sb.append("---|---:\n");
        for (final ImportingPermitApplicationSpeciesPeriodDTO speciesPeriod : model.getPeriods()) {
            sb.append(speciesName(speciesPeriod.getGameSpeciesCode()));
            sb.append('|');
            sb.append(DATE_FORMAT.print(speciesPeriod.getBeginDate()));
            sb.append(" - ");
            sb.append(DATE_FORMAT.print(speciesPeriod.getEndDate()));
            sb.append('\n');
        }
        // TODO: Should period additional information be generated onto decision, applies at lest bird applications also

        sb.append("\n\n");
    }

    private void createArea(final StringBuilder sb) {
        final String areaDescription = model.getAreaDescription();

        sb.append("---|---:\n");
        if (isNotBlank(areaDescription)) {
            cell2(sb, i18nKey("carnivore.application.area.areaDescription"), escape(areaDescription));
        }

        sb.append("\n\n");
        for (final DerogationPermitApplicationAttachmentDTO s : model.getAreaAttachments()) {
            sb.append("\\- ")
                    .append(s.getName())
                    .append("\n");
        }

        sb.append("\n\n");

    }

    private void createJustification(final StringBuilder sb) {
        sb.append(i18n("Perustelut", "Perustelut"));
        sb.append(":\n\n");

        final ImportingPermitApplicationJustificationDTO justification = model.getJustification();
        sb.append(i18n("Valtio, josta eläimet tuodaan", "Valtio, josta eläimet tuodaan"))
                .append(":\n")
                .append(escape(justification.getCountryOfOrigin()))
                .append("\n\n");
        sb.append(i18n("Tuotavien yksilöiden tarkempi kuvaus", "Tuotavien yksilöiden tarkempi kuvaus"))
                .append(":\n")
                .append(escape(justification.getDetails()))
                .append("\n\n");
        sb.append(i18n("Tuonnin tarkoitus", "Tuonnin tarkoitus"))
                .append(":\n\n")
                .append(escape(justification.getPurpose()))
                .append("\n\n");
        sb.append(i18n("Luontoon laskeminen", "Luontoon laskeminen"))
                .append(":\n\n")
                .append(escape(justification.getRelease()))
                .append("\n\n");

    }

    private void createAttachments(final StringBuilder sb) {
        if (model.getOtherAttachments().size() > 0) {
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
