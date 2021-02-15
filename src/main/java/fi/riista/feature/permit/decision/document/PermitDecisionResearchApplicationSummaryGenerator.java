package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.research.ResearchSummaryDTO;
import fi.riista.feature.permit.application.research.amount.ResearchSpeciesAmountDTO;
import fi.riista.feature.permit.application.research.period.ResearchSpeciesPeriodDTO;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
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

public class PermitDecisionResearchApplicationSummaryGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final ResearchSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionResearchApplicationSummaryGenerator(final ResearchSummaryDTO model,
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
        createReasons(sb);

        if (model.getForbiddenMethods().isForbiddenMethodSelected()) {
            createForbiddenMethods(sb);
        }

        createAttachments(sb);

        return sb.toString();
    }

    public String generateApplicationReasoning() {
        final StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(model.getJustification())) {
            sb.append(i18nKey("research.application.reasoning.heading.justification"))
                    .append("\n\n");

            sb.append(escape(model.getJustification()));
        }
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

        for (final ResearchSpeciesAmountDTO speciesAmount : model.getSpeciesAmounts()) {
            sb.append(speciesName(speciesAmount.getGameSpeciesCode()));
            sb.append('|');
            sb.append(DECIMAL_FORMAT.format(speciesAmount.getAmount()));
            sb.append(' ');
            sb.append(i18n("kpl", "st."));
            sb.append('\n');
        }
    }


    private void createPeriods(final StringBuilder sb) {
        sb.append("\n\n")
                .append(i18n(
                        "Eläinlaji ja haettu lupa-aika:",
                        "Djurart och ansökta giltighetstiden:"))
                .append("\n\n");

        sb.append("---|---:\n");
        for (final ResearchSpeciesPeriodDTO speciesPeriod : model.getSpeciesPeriods()) {
            sb.append(speciesName(speciesPeriod.getGameSpeciesCode()));
            sb.append('|');
            sb.append(DATE_FORMAT.print(speciesPeriod.getBeginDate()));
            sb.append(" - ");
            sb.append(DATE_FORMAT.print(speciesPeriod.getEndDate()));
            sb.append('\n');
        }

        sb.append("\n\n");
    }

    private void createArea(final StringBuilder sb) {
        final DerogationPermitApplicationAreaDTO areaDTO =
                model.getDerogationPermitApplicationAreaDTO();

        sb.append("\n\n").append("---|---:\n");
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

    private void createReasons(final StringBuilder sb) {
        final DerogationPermitApplicationReasonsDTO reasons = model.getReasons();

        sb.append(i18n("Poikkeusperusteet", "Grund för dispens"));
        sb.append(":\n");

        reasons.getReasons().forEach(lawSectionReason -> {
            sb.append("\n\n");
            // species
            sb.append(lawSectionReason.getSpeciesCodes());
            sb.append("\n");
            // law section
            if (lawSectionReason.getLawSection() == DerogationLawSection.SECTION_41A) {
                sb.append(i18nKey("derogation.application.reason.lawSection41a"));
                sb.append("\n");
            } else if (lawSectionReason.getLawSection() == DerogationLawSection.SECTION_41B) {
                sb.append(i18nKey("derogation.application.reason.lawSection41b"));
                sb.append("\n");
            } else if (lawSectionReason.getLawSection() == DerogationLawSection.SECTION_41C) {
                sb.append(i18nKey("derogation.application.reason.lawSection41c"));
                sb.append("\n");
            } else {
                throw new IllegalArgumentException("Illegal law section for research application:" + lawSectionReason.getLawSection());
            }
            // reasons
            lawSectionReason.getLawSectionReasons().forEach(r -> {
                if (r.isChecked()) {
                    sb.append("\\-").append(i18nKey("derogation.application.reason." + r.getReasonType()));
                    sb.append("\n");
                }
            });
        });

        sb.append("\n\n");
    }

    private void createForbiddenMethods(final StringBuilder sb) {
        sb.append(i18n("Kielletyt menetelmät", "Förbjudna fångsmetoder"));
        sb.append(":\n\n");

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection32())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.32"));
            sb.append(": ");
            sb.append("\n");
            sb.append(escape(model.getForbiddenMethods().getDeviateSection32()));
            sb.append("\n");
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection33()) || model.getForbiddenMethods().isTapeRecorders()) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.33"));
            sb.append(": ");
            sb.append("\n");

            if (model.getForbiddenMethods().isTapeRecorders()) {
                sb.append(i18nKey("bird.application.forbidden.tape"));
                sb.append("\n");
            }

            if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection33())) {
                sb.append(escape(model.getForbiddenMethods().getDeviateSection33()));
                sb.append("\n");
            }
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection34()) || model.getForbiddenMethods().isTraps()) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.34"));
            sb.append(": ");
            sb.append("\n");

            if (model.getForbiddenMethods().isTraps()) {
                sb.append(i18nKey("bird.application.forbidden.traps"));
                sb.append("\n");
            }

            if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection34())) {
                sb.append(escape(model.getForbiddenMethods().getDeviateSection34()));
                sb.append("\n");
            }
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection35())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.35"));
            sb.append(": ");
            sb.append("\n");
            sb.append(escape(model.getForbiddenMethods().getDeviateSection35()));
            sb.append("\n");
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection51())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.51"));
            sb.append(": ");
            sb.append("\n");
            sb.append(escape(model.getForbiddenMethods().getDeviateSection51()));
            sb.append("\n");
        }

        sb.append("\n");
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
