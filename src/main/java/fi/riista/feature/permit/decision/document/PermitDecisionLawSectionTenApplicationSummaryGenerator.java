package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.lawsectionten.amount.LawSectionTenPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodDTO;
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
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PermitDecisionLawSectionTenApplicationSummaryGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final LawSectionTenPermitApplicationSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionLawSectionTenApplicationSummaryGenerator(final LawSectionTenPermitApplicationSummaryDTO model,
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
        createPopulation(sb);
        return sb.toString();
    }

    private void createHeader(final StringBuilder sb) {

        sb.append(i18n(
                "Hakija on hakenut pyyntilupaa seuraavasti:",
                "Sökande har ansökt om jaktlicens enligt följande:"));

    }

    private void createAmounts(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja lupamäärä:",
                "Djurart och licensantal:"));
        sb.append("\n\n");
        sb.append("---|---:\n");

        final LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmount = model.getSpeciesAmount();
        cell2(sb, speciesName(speciesAmount.getGameSpeciesCode()),
                DECIMAL_FORMAT.format(speciesAmount.getAmount()),
                i18n("kpl", "st."));

        sb.append("\n\n");
    }


    private void createPeriods(final StringBuilder sb) {
        sb.append("\n\n")
                .append(i18n(
                        "Eläinlaji ja haettu lupa-aika:",
                        "Djurart och ansökta giltighetstiden:"))
                .append("\n\n");

        sb.append("---|---:\n");
        final LawSectionTenPermitApplicationSpeciesPeriodDTO speciesPeriod = model.getSpeciesPeriod();
        cell2(sb, speciesName(speciesPeriod.getGameSpeciesCode()),
                DATE_FORMAT.print(speciesPeriod.getBeginDate()),
                "-",
                DATE_FORMAT.print(speciesPeriod.getEndDate()));

        sb.append("\n\n");
    }

    private void createArea(final StringBuilder sb) {
        final DerogationPermitApplicationAreaDTO areaDTO =
                model.getDerogationPermitApplicationAreaDTO();

        sb.append("\n\n")
                .append(i18n("Hakemusalue:", "Ansökningsområde:"))
                .append("\n\n");

        sb.append("---|---:\n");
        cell2(sb, i18nKey("carnivore.application.area.size"), "" + areaDTO.getAreaSize() + " ha");
        final String areaDescription = areaDTO.getAreaDescription();
        if (isNotBlank(areaDescription)) {
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

    private void createPopulation(final StringBuilder sb) {
        sb.append(i18nKey("lawsectionten.application.population.populationInfo"));
        sb.append("\n\n");

        sb.append(i18nKey("lawsectionten.application.population.populationAmount"));
        sb.append("\n");

        final DerogationPermitApplicationSpeciesPopulationDTO dto = model.getPopulation();
        sb.append("\\- ");
        sb.append(speciesName(dto.getGameSpeciesCode()));
        sb.append(": ");
        sb.append(escape(dto.getPopulationAmount()));
        sb.append("\n");

        sb.append("\n");
        sb.append(i18nKey("lawsectionten.application.population.populationDescription"));
        sb.append("\n");

        sb.append("\\- ");
        sb.append(speciesName(dto.getGameSpeciesCode()));
        sb.append(": ");
        sb.append(escape(dto.getPopulationDescription()));
        sb.append("\n");

        sb.append("\n");
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
